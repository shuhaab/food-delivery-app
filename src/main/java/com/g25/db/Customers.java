package com.g25.db;

import org.json.JSONArray;
import org.json.JSONObject;

public class Customers extends DB implements DBCRUD, RetrievableById {
  public Customers() {
    super("customers");
  }

  public static void updateCredits(String customerId, double credits) {
    Customers customersDB = new Customers();
    JSONObject customer = customersDB.getById(customerId);
    customer.put("credits", credits);
    customersDB.updateFileContent();
  }

  public static String getCustomerName(String customerId) {
    Customers customersDB = new Customers();
    JSONObject customer = customersDB.getById(customerId);
    return customer.getString("name");
  }

  @Override
  public JSONObject getById(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject customer = data.getJSONObject(i);
      if (customer.getString("id").equals(id)) {
        return customer;
      }
    }
    return null;
  }

  @Override
  public void delete(String id) {
    for (int i = 0; i < data.length(); i++) {
      JSONObject customer = data.getJSONObject(i);
      if (customer.getString("id").equals(id)) {
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

    newCustomer.put("reviews", new JSONArray());
    newCustomer.put("orders", new JSONArray());
    newCustomer.put("credits", 0);

    data.put(newCustomer);

    Users usersDB = new Users();
    usersDB.add(person.getName(), "123", "customer", person.getId());
    usersDB.updateFileContent();

    updateFileContent();
  }

  @Override
  public void update(int index, JSONObject updatedObject) {
    data.put(index, updatedObject);
    updateFileContent();
  }
}
