package com.g25.Shared;

import org.json.JSONArray;

public class ComplaintInfo {
  private String orderId;
  private String title;
  private String description;
  private JSONArray replies;

  public ComplaintInfo(String orderId, String title, String description, JSONArray replies) {
    this.orderId = orderId;
    this.title = title;
    this.description = description;
    this.replies = replies;
  }

  public String getOrderId() {
    return orderId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public JSONArray getReplies() {
    return replies;
  }
}