package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Vendors extends DB implements DBCRUD, RetrievableById {
  public Vendors() {
    super("vendors");
  }

  @Override
  public void delete(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject vendor = data.getJSONObject(i);
      if (vendor.getString("id").equals(id)) {
        data.remove(i);
        break;
      }
    }
    updateFileContent();
  }

  @Override
  public void add(com.g25.admin.Person person) {
    JSONObject newVendor = new JSONObject();
    newVendor.put("id", person.getId());
    newVendor.put("name", person.getName());
    newVendor.put("email", person.getEmail());
    newVendor.put("address", person.getAddress());
    newVendor.put("phone", person.getPhone());

    newVendor.put("items", new JSONArray());
    newVendor.put("rating", 0);

    data.put(newVendor);

    Users usersDB = new Users();
    usersDB.add(person.getName(), "123", "vendor", person.getId());
    usersDB.updateFileContent();

    updateFileContent();
  }

  @Override
  public void update(int index, JSONObject updatedObject) {
    data.put(index, updatedObject);
    updateFileContent();
  }

  @Override
  public JSONObject getById(String id) {
    for (Object element : data) {
      JSONObject vendor = (JSONObject) element;
      if (vendor.getString("id").equals(id)) {
        return vendor;
      }
    }
    return null;
  }

  public static String getVendorName(String vendorId) {
    Vendors vendorsDB = new Vendors();
    JSONObject vendor = vendorsDB.getById(vendorId);
    return vendor.getString("name");
  }

  public JSONArray getItems(String vendorId) {
    Items itemsDB = new Items();
    JSONArray resultItems = new JSONArray();

    resultItems = itemsDB.getAllVendorItems(vendorId);

    return resultItems;
  }
}