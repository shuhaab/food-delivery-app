package com.g25.db;

import org.json.JSONObject;

public class Users extends DB implements RetrievableById {
  public Users() {
    super("users");
  }

  public void add(String name, String password, String role, String id) {
    JSONObject newUser = new JSONObject();
    newUser.put("name", name);
    newUser.put("password", password);
    newUser.put("role", role);
    newUser.put("id", id);

    data.put(newUser);

    updateFileContent();
  }

  public void delete(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject user = data.getJSONObject(i);
      if (user.getString("id").equals(id)) {
        data.remove(i);
        break;
      }
    }

    updateFileContent();
  }

  @Override
  public JSONObject getById(String id) {
    for (Object element : data) {
      JSONObject user = (JSONObject) element;
      if (user.getString("id").equals(id)) {
        return user;
      }
    }
    return null;
  }

  public void updatePassword(String id, String newPassword) {
    Users usersDB = new Users();
    JSONObject user = usersDB.getById(id);
    user.put("password", newPassword);
    usersDB.updateFileContent();
  }
}
