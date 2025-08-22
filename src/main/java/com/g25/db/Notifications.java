package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Notifications extends DB {
  public Notifications() {
    super("notifications");
  }

  public void addNotification(String toRole, String destinationId, String message) {
    JSONObject newNotification = new JSONObject();
    newNotification.put("toRole", toRole);
    newNotification.put("id", destinationId);
    newNotification.put("message", message);

    data.put(newNotification);
    updateFileContent();
  }

  public JSONArray getNotifications(String toRole, String destinationId) {
    JSONArray filteredNotifications = new JSONArray();

    for (Object element : data) {
      JSONObject notification = (JSONObject) element;
      if (notification.getString("toRole").equals(toRole)
          && notification.getString("id").equals(destinationId)) {
        filteredNotifications.put(notification);
      }
    }

    return filteredNotifications;
  }

  public JSONArray getCustomerNotifications(String customerId) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject notification = (JSONObject) element;
      if (notification.getString("toRole").equals("customer"))
        if (notification.getString("id").equals(customerId)) {
          result.put(notification);
        }
    }

    return result;
  }

  public JSONArray getVendorNotifications(String vendorId) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject notification = (JSONObject) element;
      if (notification.getString("toRole").equals("vendor"))
        if (notification.getString("id").equals(vendorId)) {
          result.put(notification);
        }
    }

    return result;
  }
}
