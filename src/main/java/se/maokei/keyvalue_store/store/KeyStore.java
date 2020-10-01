package se.maokei.keyvalue_store.store;

import java.util.HashMap;

/**
 * A very simple keystore(Hashmap in disguise)
 * */
public class KeyStore {
  private static KeyStore KEYSTORE;
  private HashMap<String, Object> map;

  private KeyStore() {
    map = new HashMap<String, Object>();
  }

  public static KeyStore getInstance() {
    if(KEYSTORE == null) {
      KEYSTORE = new KeyStore();
    }
    return KEYSTORE;
  }

  public Object get(String key) {
    if(map.containsKey(key)) {
      return map.get(key);
    }
    return null;
  }

  public void put(String key, Object data) {
    map.put(key, data);
  }

  public boolean keyExists(String key) {
    if(map == null) {
      return false;
    }
    return map.containsKey(key);
  }

  public void delete(String key) {
    map.remove(key);
  }

  public void clearStore() {
    map = new HashMap<String, Object>();
  }
}
