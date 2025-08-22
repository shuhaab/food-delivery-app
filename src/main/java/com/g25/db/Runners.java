package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Runners extends DB implements DBCRUD, RetrievableById {
  public Runners() {
    super("runners");
  }

  public void declineTask(String runnerId, String orderId) {
    JSONObject runner = getById(runnerId);

    Orders ordersdb = new Orders();
    ordersdb.addRunnerToDeclined(orderId, runnerId);

    JSONArray tasksArray = runner.getJSONArray("tasks");
    for (int i = 0; i < tasksArray.length(); i++) {
      if (tasksArray.getString(i).equals(orderId)) {
        tasksArray.remove(i);
        break;
      }
    }

    String availableRunner = getAvailableRunner();
    JSONObject order = ordersdb.getById(orderId);
    if (availableRunner == null) {
      order.put("runnerId", JSONObject.NULL);
      order.put("status", "cancelled");
    } else {
      for (Object declinedByElement : order.getJSONArray("declinedBy")) {
        String declinedById = (String) declinedByElement;
        if (declinedById.equals(availableRunner)) {
          // set runnerid to null and status to cancelled
          order.put("runnerId", JSONObject.NULL);
          order.put("status", "cancelled");

          ordersdb.updateFileContent();

          // notify customer that their order was cancelled
          Notifications notificationsDB = new Notifications();
          notificationsDB.addNotification("customer",
              order.getString("customerId"),
              "Your order has been cancelled because there was no runner available.");

          // refund customer
          Customers customersDB = new Customers();
          JSONObject customerObject = customersDB.getById(order.getString("customerId"));
          customerObject.put("credits",
              customerObject.getDouble("credits") + order.getDouble("total") + order.getDouble("deliveryFee"));
          customersDB.updateFileContent();
          return;
        }
      }
      order.put("runnerId", availableRunner);
      JSONObject selectedRunner = getById(availableRunner);
      selectedRunner.getJSONArray("tasks").put(orderId);
    }

    updateFileContent();
    ordersdb.updateFileContent();

    System.out.println("task declined");
  }

  public void acceptTask(String runnerId, String orderId) {
    Orders ordersdb = new Orders();
    Notifications notificationsDB = new Notifications();

    JSONObject selectedOrder = ordersdb.getById(orderId);
    selectedOrder.put("status", "ongoing");

    ordersdb.updateFileContent();

    notificationsDB.addNotification("customer", selectedOrder.getString("customerId"),
        "Your order has been accepted by a driver!");

    System.out.println("task accepted");
  }

  public void completeTask(String runnerId, String orderId) {
    Orders ordersdb = new Orders();
    Notifications notificationsDB = new Notifications();

    JSONObject selectedOrder = ordersdb.getById(orderId);
    selectedOrder.put("status", "completed");
    ordersdb.updateFileContent();

    notificationsDB.addNotification("customer", selectedOrder.getString("customerId"),
        "Your order has been completed!");

    System.out.println("task completed");
  }

  public static String getAvailableRunner() {
    Runners runnersDB = new Runners();
    Orders ordersDB = new Orders();

    for (Object runnerElement : runnersDB.data) {
      JSONObject runnerObject = (JSONObject) runnerElement;

      int completedTasks = 0;

      JSONArray tasksArray = runnerObject.getJSONArray("tasks");
      if (tasksArray.length() == 0) {
        return runnerObject.getString("id");
      }

      for (Object taskElement : tasksArray) {
        String taskId = (String) taskElement;
        JSONObject task = ordersDB.getById(taskId);

        if (task.getString("status").equals("completed")) {
          completedTasks++;
        }
      }

      if (completedTasks == tasksArray.length()) {
        return runnerObject.getString("id");
      }
    }

    return null;
  }

  @Override
  public JSONObject getById(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject runner = data.getJSONObject(i);
      if (runner.getString("id").equals(id)) {
        return runner;
      }
    }
    return null;
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
    JSONObject newCustomer = new JSONObject();
    newCustomer.put("id", person.getId());
    newCustomer.put("name", person.getName());
    newCustomer.put("email", person.getEmail());
    newCustomer.put("address", person.getAddress());
    newCustomer.put("phone", person.getPhone());

    newCustomer.put("tasks", new JSONArray());
    newCustomer.put("rating", 0);

    data.put(newCustomer);

    Users usersDB = new Users();
    usersDB.add(person.getName(), "123", "runner", person.getId());
    usersDB.updateFileContent();

    updateFileContent();
  }

  @Override
  public void update(int index, JSONObject updatedObject) {
    data.put(index, updatedObject);
    updateFileContent();
  }
}
