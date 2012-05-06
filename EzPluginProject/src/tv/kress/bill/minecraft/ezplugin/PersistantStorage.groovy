package tv.kress.bill.minecraft.ezplugin

import java.util.HashMap;
import java.util.logging.Logger;

// Don't make new instances of this class--at least for now the hashes will overwrite each other.
class PersistantStorage {
    private final static String DATA_FILE_NAME = "PersistedData.dat";
    private HashMap<String, Serializable> storage;
    File Data;
    Logger log;

    public void init(File dataFolder, Logger log) {
        this.log = log;

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            log.info("Creating directory"+dataFolder)
        }
        dataFolder = new File(dataFolder, ".persisted_data")
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
            log.info("Creating Directory"+dataFolder)
        }
        data = new File(dataFolder, DATA_FILE_NAME);

        try {
            ObjectInputStream ois;
            ois = new ObjectInputStream(new FileInputStream(data));
            storage = (HashMap<String, Serializable>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            storage = new HashMap<String, Serializable>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info "read: "+storage
    }

    public Object get(String group, String key) {
        return storage.get(group + ":" + key);
    }

    public void put(String group, String key, Object value) {
        storage.put(group + ":" + key,  value);
    }
    public boolean containsKey(String group, String key) {
        return storage.containsKey(group + ":" + key);
    }
    public void save() {
        log.info "saving : "+storage
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(data));
            oos.writeObject(storage);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
