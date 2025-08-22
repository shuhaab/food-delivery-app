package com.g25.db;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.*;

public abstract class DB {
  protected String filePath;
  protected JSONArray data = new JSONArray();
  protected String fileName;

  public DB(String fileName) {
    this.fileName = fileName;
    refreshData();
  }

  public void refreshData() {
    try {
      this.filePath = "src/main/resources/com/g25/db/" + fileName + ".txt";

      String fileContent = Files.readString(Path.of(this.filePath));
      this.data = new JSONArray(fileContent);
    } catch (IOException e) {
      System.out.println("Error reading file");
      e.printStackTrace();
    }
  }

  public JSONArray getDetachedData() {
    return new JSONArray(data.toString());
  }

  public JSONArray getData() {
    return data;
  }

  public void setData(JSONArray newData) {
    this.data = new JSONArray(newData.toString());
  }

  /**
   * Writes the update data to the text file
   */
  public void updateFileContent() {
    try {
      FileWriter file = new FileWriter(filePath);
      file.write(this.data.toString());
      file.close();
      refreshData();
    } catch (IOException e) {
      System.out.println("Error writing file");
      e.printStackTrace();
    }
  }
}
