package com.g25.vendor;

public class ItemPrice {
  private String id;
  private String item;
  private double price;

  public ItemPrice(String id, String item, double price) {
    this.id = id;
    this.item = item;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }
}
