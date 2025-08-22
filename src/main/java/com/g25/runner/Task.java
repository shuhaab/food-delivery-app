package com.g25.runner;

import java.util.Map;

public class Task {
  private String taskId;
  private String customerId;
  private Map<String, Integer> items;
  private String status;

  public Task(String taskId, String customerId, Map<String, Integer> items, String status) {
    this.taskId = taskId;
    this.customerId = customerId;
    this.items = items;
    this.status = status;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public void setCustomerId(String customerId) {
    this.customerId = customerId;
  }

  public Map<String, Integer> getItems() {
    return items;
  }

  public void setItems(Map<String, Integer> items) {
    this.items = items;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
