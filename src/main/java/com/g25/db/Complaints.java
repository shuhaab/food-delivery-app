package com.g25.db;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class Complaints extends DB implements RetrievableById {
  public Complaints() {
    super("complaints");
  }

  @Override
  public JSONObject getById(String id) {
    for (Object element : data) {
      JSONObject complaint = (JSONObject) element;
      if (complaint.getString("id").equals(id)) {
        return complaint;
      }
    }
    return null;
  }

  public JSONArray getCustomerComplaints(String customerId) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject complaint = (JSONObject) element;
      if (complaint.getString("customerId").equals(customerId)) {
        result.put(complaint);
      }
    }

    return result;
  }

  public void addComplaint(String customerId, String title, String description, String orderId) {
    JSONObject newComplaint = new JSONObject();
    String currentDate = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

    newComplaint.put("date", currentDate);
    newComplaint.put("complaintTitle", title);
    newComplaint.put("description", description);
    newComplaint.put("replies", new JSONArray());
    newComplaint.put("orderId", orderId);
    newComplaint.put("customerId", customerId);
    newComplaint.put("status", "pending");
    newComplaint.put("id", UUID.randomUUID().toString());

    data.put(newComplaint);
    updateFileContent();
  }
}
