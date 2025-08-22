package com.g25.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HumanDate {
  public static String convert(String dateString) {
    SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");

    try {
      Date date = inputFormat.parse(dateString);
      String formattedDate = outputFormat.format(date);

      return formattedDate;
    } catch (ParseException e) {
      e.printStackTrace();
    }

    return null;
  }
}
