package tv.kress.bill.minecraft.ezplugin;

import java.util.*;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExceptionNoStack;

/**
 * TODO: Be able to set scan interval (based on yml variable & changeable by
 * command line)
 * 
 * TODO: Be able to set "Save" interval, currently save only happens at disable
 * 
 * TODO: test failure responses at both compile and run time.
 * 
 * TODO: (Maybe) Log errors to disk next to the script (same file
 * name).stackDump or something
 * 
 * TODO: Settings yaml file! Put imports in there.
 * 
 * @author Bill Kress
 * 
 */
public class EzPlugin extends JavaPlugin {
    Logger log;
    //TODO: FIX THIS after we figure out of it even works.
    public static EzPlugin myInstance;
    static String path = "plugins/EzPlugin/";
    // Numbers of ticks per second actually, but it reads better as 10 * SECOND
    private final long SECOND = 20L;

    private PluginManager pm;

    private DiskWatcher diskWatcher;

    public final PersistantStorage pStorage = new PersistantStorage();
    private final CommandAnnotationHandler commands = new CommandAnnotationHandler();
    private final PersistenceAnnotationHandler persistence = new PersistenceAnnotationHandler();
    private final InjectAnnotationHandler injection = new InjectAnnotationHandler();

    /**
     * When we are starting up, just kick off the DiskWatcher thread, it will
     * let us know of all the "New" classes on the disk by calling updatePlugins
     */
    @Override
    public void onEnable() {
        myInstance = this;

        if (log == null)
            log = this.getLogger();

        pStorage.init(this.getDataFolder(), this.getLogger());

        persistence.init();

        pm = Bukkit.getPluginManager();
        diskWatcher = new DiskWatcher(this);

        getServer().getScheduler().scheduleAsyncRepeatingTask(this, diskWatcher, SECOND, 1 * SECOND);
    }

    /**
     * Need to get the list of current scripts from DiskWatcher so that we can
     * call onDisable on each script then persist the persistable fields.
     * <p>
     * When we disable we assume the scheduler will stop calling diskWatcher,
     * but we need to grab the current set of scripts from diskWatcher by
     * synchronously calling toAdd with a null (which will put all current
     * scripts in toRemove), then manually call update with the list of removed
     * plugins.
     * <p>
     * This seemed like a good solution. I considered just telling the
     * DiskWatcher to send it's list of all scripts through the "Normal"
     * channels, but it normally runs async and I don't think it's a good idea
     * to wait for the callback since we are being disabled right now. this also
     * avoids potential timing conflicts with the scheduler.
     * 
     */
    @Override
    public void onDisable() {
        // Stop the running scripts--mostly gives persistence a chance to save
        // data.
        Set<ScriptFile> toRemove = new HashSet<ScriptFile>();
        // Special case, gets all running scripts.
        diskWatcher.scan(null, toRemove);
        updatePlugins(new HashSet<ScriptFile>(), toRemove);
        persistence.save();
        log.info("EzPlugin Disabled");
    }

    /**
     * This is called by DiskWatcher when files on the disk change. ALL plugins
     * are started and stopped from this method
     * 
     * @param toAdd
     *            set of plugins to turn on
     * @param toRemove
     *            set of plugins to turn off
     */
    public void updatePlugins(Set<ScriptFile> toAdd, Set<ScriptFile> toRemove) {
        for (ScriptFile sf : toRemove) {
            log.finest("Script " + sf.getName() + " outdated or removed, unloading");
            remove(sf);
        }

        injection.updateRecords(toRemove, toAdd);

        for (ScriptFile sf : toAdd) {
            log.finest("Script " + sf.getName() + " new or updated, loading");
            add(sf);
        }
    }

    private void add(ScriptFile sf) {
        if (sf.getInstance() == null)
            return;

        Object script = sf.getInstance();

        // Before onEnable, let's prepare the script's state.
        if (script instanceof Listener)
            pm.registerEvents((Listener) script, this);

        // Scan the script and insert manage @Persist annotations
        persistence.scan(script);

        injection.scan(script);

        // Call onEnable.
        try {
            sf.getInstance().invokeMethod("onEnable", null);
        } catch (MissingMethodExceptionNoStack e) {
            // YES, I really don't care if the method doesn't exist and want
            // to silently eat this exception.
        }

        // After enabled, we can set up the commands
        List<String> newCommands = commands.addCommandsFor(script);
        for (String cmd : newCommands)
            log.finest("Added New Command handler: " + cmd);
    }

    private void remove(ScriptFile sf) {
        // If the file failed to parse, the Instance will be null. Just ignore it
        if (sf.getInstance() == null) {
            return;
        }

        try {
            sf.getInstance().invokeMethod("onDisable", null);
        } catch (MissingMethodExceptionNoStack e) {
            // YES, I really don't care if this optional method doesn't exist-I want to silently eat this exception.
        }

        Object script = sf.getInstance();

        if (script instanceof Listener)
            HandlerList.unregisterAll((Listener) script);

        commands.remove(script);
        persistence.remove(script);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equals("ezpz")) {
            diskWatcher.run();
            return true;
        } else if (commandLabel.equals("ezp")) {
            if (args.length == 0)
                sender.sendMessage(commands.listCommands());
            else {
                String err = commands.executeCommand(sender, args);
                if (err != null)
                    sender.sendMessage(err);
            }
            return true;
        } else
            return false;
    }

}
