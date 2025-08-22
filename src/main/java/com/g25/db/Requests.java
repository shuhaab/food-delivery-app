package com.g25.db;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class Requests extends DB implements RetrievableById {
  public Requests() {
    super("requests");
  }

  @Override
  public JSONObject getById(String requestId) {
    for (Object element : data) {
      JSONObject request = (JSONObject) element;
      if (request.getString("id").equals(requestId)) {
        return request;
      }
    }
    return null;
  }

  public static JSONArray getCustomerRequests(String customerId) {
    Requests requestsDB = new Requests();
    JSONArray result = new JSONArray();

    for (Object element : requestsDB.data) {
      JSONObject request = (JSONObject) element;
      if (request.getString("customerId").equals(customerId)) {
        result.put(request);
      }
    }

    return result;
  }

  public void addRequest(String customerId, double topupValue) {
    JSONObject request = new JSONObject();

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    String currentDate = sdf.format(new Date());

    request.put("id", UUID.randomUUID().toString());
    request.put("date", currentDate);

    request.put("status", "pending");
    request.put("customerId", customerId);

    request.put("topupValue", topupValue);

    data.put(request);
    updateFileContent();
  }

  public void acceptRequest(String requestId) {
    JSONObject request = getById(requestId);

    Customers customersDB = new Customers();
    JSONObject selectedCustomer = customersDB.getById(request.getString("customerId"));
    selectedCustomer.put("credits", selectedCustomer.getDouble("credits") + request.getDouble("topupValue"));

    request.put("status", "processed");

    Notifications notificationsDB = new Notifications();
    notificationsDB.addNotification("customer", request.getString("customerId"),
        "Your request to top up " + request.getDouble("topupValue") + " credits has been processed!");

    customersDB.updateFileContent();
    updateFileContent();
  }
}