package tv.kress.bill.minecraft.ezplugin;

import groovy.lang.*;
import groovy.util.GroovyScriptEngine;

import java.io.File;
import java.util.*;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Holds a reference to a script and it's last updated date.
 * 
 * As long as two of these objects represent the same file version (including
 * last updated), they will be equal.
 * 
 * @author Bill Kress
 * 
 */
class ScriptFile {
    private static GroovyScriptEngine gse;

    private final File file;
    private final long modifiedDate;
    private Object instance;
    private boolean parsingAttempted = false;

    public ScriptFile(File file) {
        // I believe the Groovy class loader should work as written, but
        // currently it never recompiles the class.

        this.file = file;
        modifiedDate = file.lastModified();
    }

    private void parse() {
        Class<?> scriptClass;
        try {
            if (gse == null) {
                CompilerConfiguration cconfig = new CompilerConfiguration();

                if (true) {
                    ImportCustomizer ic = new ImportCustomizer();
                    ic.addStarImports( //
                                    "org.bukkit", //
                                    "org.bukkit.command", //
                                    "org.bukkit.entity", //
                                    "org.bukkit.event", //
                                    "org.bukkit.event.block", //
                                    "org.bukkit.event.entity", //
                                    "org.bukkit.event.inventory", //
                                    "org.bukkit.event.player", //
                                    "org.bukkit.event.server", //
                                    "org.bukkit.event.vehicle", //
                                    "org.bukkit.event.weather", //
                                    "org.bukkit.event.world", //
                                    "org.bukkit.inventory", //
                                    "org.bukkit.map", //
                                    "org.bukkit.material", //
                                    "org.bukkit.permissions", //
                                    "tv.kress.bill.minecraft.ezplugin" //

                    );
                    ic.addImports("groovy.transform.Field");
                    ic.addStaticStars("org.bukkit.Material", "org.bukkit.entity.EntityType");
                    //                    ic.addImports("org.bukkit.event.EventHandler", "org.bukkit.event.Listener", "org.bukkit.event.block.Action", "org.bukkit.event.player.PlayerInteractionEvent", "org.bukkit.Material");
                    cconfig.addCompilationCustomizers(ic);
                }
                cconfig.setScriptBaseClass("tv.kress.bill.minecraft.ezplugin.ScriptBase");

                GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader(), cconfig);

                // Set up the classpath for the scripts
                File plugins = new File("plugins/");
                List<String> classpath = new ArrayList<String>();
                classpath.add("craftbukkit.jar");
                classpath.add("./plugins/EzPlugin/");
                // Scan the plugins folder and add all jars
                for (File f : plugins.listFiles()) {
                    if (f.getAbsolutePath().endsWith(".jar")) {
                        classpath.add(f.getAbsolutePath());
                        System.out.println("Adding classpath entry for:" + f.getAbsolutePath());
                    }
                }
                String[] cpString = classpath.toArray(new String[] {});
                gse = new GroovyScriptEngine(cpString, gcl);
            }

            scriptClass = gse.loadScriptByName(file.getAbsolutePath());

            instance = scriptClass.newInstance();

        } catch (Exception e) {
            System.out.println("Error parsing " + file.getName() + ":" + e.getMessage());
        }
    }

    public String getName() {
        return file.getName();
    }

    /**
     * This may be null if the class failed to parse.
     * 
     * @return
     */
    public GroovyObject getInstance() {
        if (!parsingAttempted) {
            parsingAttempted = true;
            parse();
        }
        return (GroovyObject) instance;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        result = prime * result + (int) (modifiedDate ^ (modifiedDate >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScriptFile other = (ScriptFile) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        if (modifiedDate != other.modifiedDate)
            return false;
        return true;
    }

}
