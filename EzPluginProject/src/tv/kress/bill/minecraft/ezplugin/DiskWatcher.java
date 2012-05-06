package tv.kress.bill.minecraft.ezplugin;

import java.io.File;
import java.util.*;

/**
 * The same instance of this class can be used to scan both threaded and
 * synchronous.
 * <p>
 * The threaded version is expected to have run() called now and then, the
 * parent will be notified in the correct thread at a later point of any
 * changes.
 * <p>
 * scan() can be called at any time to do a manual scan from any thread. This
 * should work even if run() is called at the same time--or just call run()
 * itself and wait for a response
 * 
 * @author Bill Kress
 * 
 */
class DiskWatcher implements Runnable {
    private final Set<ScriptFile> knownScripts = new HashSet<ScriptFile>();
    private EzPlugin parent;

    public DiskWatcher() {

    }

    public DiskWatcher(EzPlugin parent) {
        this.parent = parent;
    }

    @Override
    public void run() {
        final Set<ScriptFile> toAdd = new HashSet<ScriptFile>();
        final Set<ScriptFile> toRemove = new HashSet<ScriptFile>();

        scan(toAdd, toRemove);

        if (toAdd.size() != 0 || toRemove.size() != 0) {
            System.out.println("Change detected: add:" + toAdd.size() + ", remove:" + toRemove.size());
            parent.getServer().getScheduler().scheduleSyncDelayedTask(parent, new Runnable() {
                @Override
                public void run() {
                    parent.updatePlugins(toAdd, toRemove);
                }
            });
        }
    }

    /**
     * Scans the selected directory for changes. toAdd will contain files that
     * have been added, toRemove will contain files that need to be removed. If
     * a file has been touched, it will appear in both lists.
     * 
     * @param toAdd
     *            List of files that have appeared in the directory or changed
     * @param toRemove
     *            List of files that have been removed or changed
     */
    public synchronized void scan(Set<ScriptFile> toAdd, Set<ScriptFile> toRemove) {
        Set<ScriptFile> newScripts = scanScripts(parent.getDataFolder(), "groovy");

        // Special case, returns all active scripts.
        if (toAdd == null) {
            toRemove.addAll(knownScripts);
            return;
        }

        // We need to remove listeners for all scripts that used to exist
        toRemove.addAll(knownScripts);
        // Except the ones that still exist
        toRemove.removeAll(newScripts);

        // Add new scripts.
        toAdd.addAll(newScripts);
        // If they weren't already there
        toAdd.removeAll(knownScripts);

        // Previously I had knownScripts = newScripts, but it caused problematic
        // recompiles.
        knownScripts.removeAll(toRemove);
        knownScripts.addAll(toAdd);
    }

    /**
     * Reads the given directory (and all sub-directories) and creates a set of
     * "ScriptFile" objects. These can be compared to a previous set to find
     * changes (ScriptFile objects evaluate .equals based on filename and last
     * modified date)
     * 
     * @param topDirectory
     * @param type
     * @return
     */
    Set<ScriptFile> scanScripts(File topDirectory, String type) {
        Set<ScriptFile> allScripts = new HashSet<ScriptFile>();

        Queue<File> fileQueue = new LinkedList<File>();
        fileQueue.add(topDirectory);

        File file;
        while ((file = fileQueue.poll()) != null) {
            if (file.isFile() && file.getName().endsWith("." + type)) {
                ScriptFile sf = new ScriptFile(file);
                allScripts.add(sf);

            } else if (file.isDirectory()) {
                fileQueue.addAll(Arrays.asList(file.listFiles()));
            }
        }
        return allScripts;
    }

}