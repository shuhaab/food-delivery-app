package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Items extends DB implements RetrievableById {
  public Items() {
    super("items");
  }

  public JSONArray getAllVendorItems(String vendorId) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject item = (JSONObject) element;
      if (item.getString("vendorId").equals(vendorId)) {
        if (!item.getBoolean("removed")) {
          result.put(item);
        }
      }
    }

    return result;
  }

  public static String getItemName(String itemId) {
    Items itemsDB = new Items();
    for (Object element : itemsDB.data) {
      JSONObject json = (JSONObject) element;
      if (json.getString("id").equals(itemId)) {
        return json.getString("itemName");
      }
    }

    return null;
  }

  public void AddItem(String itemName, double price, String vendorId) {
    String id = java.util.UUID.randomUUID().toString();
    JSONObject newItem = new JSONObject();
    newItem.put("id", id);
    newItem.put("itemName", itemName);
    newItem.put("price", price);
    newItem.put("vendorId", vendorId);
    newItem.put("removed", false);

    data.put(newItem);
    updateFileContent();
  }

  public void removeItem(String itemId) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject item = data.getJSONObject(i);
      if (item.getString("id").equals(itemId)) {
        item.put("removed", true);
        break;
      }
    }
    updateFileContent();
  }

  @Override
  public JSONObject getById(String id) {
    Items itemsDB = new Items();
    for (Object element : itemsDB.data) {
      JSONObject item = (JSONObject) element;
      if (item.getString("id").equals(id)) {
        return item;
      }
    }

    return null;
  }
}
