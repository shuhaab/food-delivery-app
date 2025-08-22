package com.g25.db;

import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

public class Reviews extends DB {
  public Reviews() {
    super("reviews");
  }

  public enum ReviewType {
    ORDER, VENDOR, RUNNER;

    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }

  public double getAverageRating(ReviewType type, String id) {
    JSONArray reviews = getReviewsByTypeAndId(type, id);

    double sum = 0;
    for (Object element : reviews) {
      JSONObject review = (JSONObject) element;
      sum += review.getDouble("rating");
    }

    return sum / reviews.length();
  }

  public JSONArray getReviewsByTypeAndId(ReviewType type, String id) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject review = (JSONObject) element;
      if (review.getString("type").equals(type.toString())) {
        if (review.getString(type.toString() + "Id").equals(id)) {
          result.put(review);
        }
      }
    }

    return result;
  }

  public JSONArray getSenderReviews(String senderId) {
    JSONArray result = new JSONArray();

    for (Object element : data) {
      JSONObject review = (JSONObject) element;
      if (review.getString("sender").equals(senderId)) {
        result.put(review);
      }
    }

    return result;
  }

  public void addReview(ReviewType type, String senderId, int rating, String comment,
      String reviewedEntityId) throws IllegalArgumentException {
    JSONObject review = new JSONObject();

    review.put("id", UUID.randomUUID().toString());
    review.put("type", type.toString());
    review.put(type.toString() + "Id", reviewedEntityId);
    review.put("sender", senderId);
    review.put("rating", rating);
    review.put("comment", comment);
    review.put(type.toString() + "Id", reviewedEntityId);

    data.put(review);
    updateFileContent();

    if (type == ReviewType.VENDOR) {
      Vendors vendorsDB = new Vendors();
      vendorsDB.getById(reviewedEntityId).put("avgRating", this.getAverageRating(type, reviewedEntityId));
      vendorsDB.updateFileContent();
    } else if (type == ReviewType.RUNNER) {
      Runners runnersDB = new Runners();
      runnersDB.getById(reviewedEntityId).put("avgRating", this.getAverageRating(type, reviewedEntityId));
      runnersDB.updateFileContent();
    }
  }
}
