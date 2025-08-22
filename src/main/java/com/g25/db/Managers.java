package com.g25.db;

import org.json.JSONObject;

public class Managers extends DB implements RetrievableById {
  public Managers() {
    super("managers");
  }

  @Override
  public JSONObject getById(String id) {
    for (Object element : data) {
      JSONObject manager = (JSONObject) element;
      if (manager.getString("id").equals(id)) {
        return manager;
      }
    }
    return null;
  }

  public static String getManagerName(String managerId) {
    Managers managersDB = new Managers();
    JSONObject manager = managersDB.getById(managerId);
    return manager.getString("name");
  }
}
