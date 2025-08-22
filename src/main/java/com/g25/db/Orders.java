package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class Orders extends DB implements RetrievableById {
  public enum OrderType {
    DELIVERY, DINEIN, TAKEAWAY;

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  public Orders() {
    super("orders");
  }

  public JSONArray getAllCompletedOrders() {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject orderObject = (JSONObject) element;

      if (orderObject.getString("status").equals("completed")) {
        result.put(orderObject);
      }
    }

    return result;
  }

  @Override
  public JSONObject getById(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject order = data.getJSONObject(i);
      if (order.getString("id").equals(id)) {
        return order;
      }
    }
    return null;
  }

  public void addRunnerToDeclined(String orderId, String runnerId) {
    JSONObject order = getById(orderId);

    JSONArray declinedBy = order.getJSONArray("declinedBy");
    declinedBy.put(runnerId);

    // updateFileContent();
  }

  public JSONArray getCustomerOrders(String customerId) {
    JSONArray orders = new JSONArray();

    for (Object element : data) {
      JSONObject order = (JSONObject) element;
      if (order.getString("customerId").equals(customerId)) {
        orders.put(order);
      }
    }

    return orders;
  }

  public void createNewOrder(String vendorId, String customerId, OrderType orderType, List<String> itemIds,
      double total) {
    DecimalFormat df = new DecimalFormat("0.00");
    Customers customersDB = new Customers();
    Notifications notificationsDB = new Notifications();

    JSONObject order = new JSONObject();
    JSONArray itemsArray = new JSONArray();

    itemIds.forEach(itemId -> itemsArray.put(itemId));

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    String currentDate = sdf.format(new Date());

    double deliveryFee = total * 0.1;
    deliveryFee = Double.parseDouble(df.format(deliveryFee));

    order.put("date", currentDate);
    order.put("orderType", orderType.toString());
    order.put("total", total);
    order.put("deliveryFee", deliveryFee);
    order.put("customerId", customerId);
    order.put("vendorId", vendorId);
    order.put("items", itemsArray);

    order.put("runnerId", JSONObject.NULL);

    order.put("declinedBy", new JSONArray());

    String orderUUID = UUID.randomUUID().toString();
    order.put("id", orderUUID);

    JSONArray items = new JSONArray();
    for (String itemId : itemIds) {
      items.put(itemId);
    }

    order.put("status", "wait_vendor");

    data.put(order);
    updateFileContent();

    Double currentCustomerCredits = customersDB.getById(customerId).getDouble("credits");

    customersDB.getById(customerId).put("credits",
        Double.parseDouble(df.format(currentCustomerCredits - (total + deliveryFee))));
    customersDB.getById(customerId).getJSONArray("orders").put(orderUUID);
    customersDB.updateFileContent();

    notificationsDB.addNotification("vendor", vendorId, "New order placed by customer");
  }

  public void acceptOrder(String orderId) {
    JSONObject order = this.getById(orderId);
    Notifications notificationsDB = new Notifications();
    Runners runnersDB = new Runners();

    String availableRunner = Runners.getAvailableRunner();
    if (availableRunner == null) {
      order.put("runnerId", JSONObject.NULL);
      order.put("status", "cancelled");

      notificationsDB.addNotification("customer", order.getString("customerId"),
          "Your order has been cancelled because there was no available runners");
    } else {
      order.put("runnerId", availableRunner);
      order.put("status", "wait_runner");

      JSONObject selectedRunner = runnersDB.getById(availableRunner);
      selectedRunner.getJSONArray("tasks").put(orderId);
      runnersDB.updateFileContent();

      notificationsDB.addNotification("customer", order.getString("customerId"),
          "Your order has been accepted by the vendor");
    }

    updateFileContent();
  }
}