package tv.kress.bill.minecraft.ezplugin;

import java.lang.reflect.Field;

/**
 * VFP--Very Flaky Persistence.
 * 
 * First thing to fix, have it store each plugin under it's own name.dat and
 * retrieve it only when needed. Second thing, Store separate data for each
 * plugin x user. Step 3, consider a DB.
 * 
 * @author Bill Kress
 * 
 */
class PersistenceAnnotationHandler {
    private PersistantStorage storage;

    public void init() {
        storage = EzPlugin.myInstance.pStorage;
    }

    public void scan(Object target) {
        for (Field f : target.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(tv.kress.bill.minecraft.ezplugin.Persist.class)) {
                retrieve(target, f);
            }
        }
    }

    public void remove(Object target) {
        for (Field f : target.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(tv.kress.bill.minecraft.ezplugin.Persist.class)) {
                System.out.println("Saving field: " + f.getName() + " value= " + target.getClass().toString());
                store(target, f);
            }
        }
    }

    private String mkGrp(Object target) {
        String name = target.getClass().getCanonicalName();
        return name;
    }

    private String mkName(Field f) {
        String name = f.getName() + ":" + f.getType().toString();
        return name;
    }

    private void retrieve(Object target, Field f) {
        if (!storage.containsKey(mkGrp(target), mkName(f)))
            return;
        try {
            f.setAccessible(true);
            Object storedValue = storage.get(mkGrp(target), mkName(f));
            f.set(target, storedValue);
            System.out.printf("Restoring field " + f.getName() + " for " + target.getClass().toString() + " (" + storedValue + ")");
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        storage.save();
    }

    private void store(Object target, Field f) {
        try {
            f.setAccessible(true);
            storage.put(mkGrp(target), mkName(f), f.get(target));
            System.out.printf("Saving field " + f.getName() + " for " + target.getClass().toString() + " (" + f.get(target) + ")");
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}