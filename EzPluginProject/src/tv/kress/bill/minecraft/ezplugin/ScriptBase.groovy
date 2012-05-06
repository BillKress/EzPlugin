package tv.kress.bill.minecraft.ezplugin

import java.util.logging.Logger;

import groovy.lang.Script;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin

public class ScriptBase extends Script implements Listener  {

    public Plugin getPlugin() {
        return EzPlugin.myInstance
    }

    public Logger getLogger() {
        return plugin.logger
    }

    public void log (String log) {
        logger.info("["+this.class.name+"] " + log)
    }

    @Override
    public Object run() {
        throw new IllegalStateException("Shouldn't call run in the base script");
    }

    /**
     * Allows a script to persist a hash for a user.  Actually any
     * string will work as a "Key". <p>
     * <p>
     * the closure will be executed right away and the hash will be passed in, when
     * the closure exits, the hash will be persisted until next time.
     * <p>
     * @param key Each key specifies a unique hash that will be passed into the closure
     * @param func The closure to execute, hash will be passed as parameter
     */
    def persistScope(String key, Closure func) {
        def pers=EzPlugin.myInstance.pStorage;

        def storage = pers.get(this.class.name, key);
        if(storage == null) {
            storage = [:]
        }
        func(storage)
        pers.put(this.class.name, key, storage)
    }
}
