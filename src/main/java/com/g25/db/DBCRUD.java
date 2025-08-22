package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public interface DBCRUD {
  JSONArray getDetachedData();

  void add(com.g25.admin.Person person);

  void delete(String id);

  void update(int index, JSONObject updatedObject);
}
