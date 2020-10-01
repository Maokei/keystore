package se.maokei.keyvalue_store.model;

public class StoreAction {
  public String cmd;
  public String key;
  public String data;

  public StoreAction(String cmd, String key, String data) {
    this.cmd = cmd;
    this.key = key;
    this.data = data;
  }

  public String getCmd() {
    return cmd;
  }

  public void setCmd(String cmd) {
    this.cmd = cmd;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
