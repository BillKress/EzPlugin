package tv.kress.bill.minecraft.ezplugin;

import java.lang.reflect.Field;
import java.util.*;

class InjectAnnotationHandler {

    private final HashMap<String, ScriptFile> scripts = new HashMap<String, ScriptFile>();

    public void updateRecords(Set<ScriptFile> toRemove, Set<ScriptFile> toAdd) {
        for (ScriptFile sf : toRemove)
            // When a class is removed, all @Inject references to that class should probably be nulled.
            // but that adds quite a bit of complexity--for now I think this will just leave a
            // ghost of a class after the file has been deleted (but modifying should be absolutely fine).
            if (sf.getInstance() != null)
                scripts.remove(sf.getInstance().getClass());
        for (ScriptFile sf : toAdd)
            if (sf.getInstance() != null) {
                //   System.out.println("Adding script to cache:" + sf.getInstance().getClass().toString());
                scripts.put(sf.getInstance().getClass().getName(), sf);
            }
    }

    /**
     * updateRecords must resolve ALL adds and removes before scan can be
     * called. this allows two classes to reverence each other without a
     * problem.
     * 
     * @param target
     */
    public void scan(Object target) {
        for (Field f : target.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(tv.kress.bill.minecraft.ezplugin.Inject.class)) {
                try {
                    ScriptFile sf = scripts.get(f.getType().getName());
                    if (sf == null) {
                        System.out.println("WARNING: inject class not found:" + f.getType().getName());
                        continue;
                    }
                    f.setAccessible(true);
                    f.set(target, sf.getInstance());
                    System.out.println("Injected " + sf.getInstance().getClass() + " into field " + f.getName() + " of " + target.getClass());
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}