package com.g25;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import com.g25.db.Runners;
import com.g25.db.Reviews.ReviewType;
import com.g25.Utils.HumanDate;
import com.g25.customcomponent.CellContainer;
import com.g25.customcomponent.Navbar;
import com.g25.db.Customers;
import com.g25.db.Items;
import com.g25.db.Orders;
import com.g25.db.Reviews;
import com.g25.runner.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

class Review {
    private String id;
    private String comment;
    private Integer rating;
    private String sender;

    public Review(String id, String comment, Integer rating, String sender) {
        this.id = id;
        this.comment = comment;
        this.rating = rating;
        this.sender = sender;
    }

    public String getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }

    public Integer getRating() {
        return rating;
    }

    public String getSender() {
        return sender;
    }
}

class RevenueInfo {
    private String orderId;
    private String date;
    private Integer revenue;

    public RevenueInfo(String orderId, String date, Integer revenue) {
        this.orderId = orderId;
        this.date = date;
        this.revenue = revenue;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDate() {
        return date;
    }

    public Integer getRevenue() {
        return revenue;
    }
}

public class RunnerController {

    @FXML
    private HBox navbarContent;

    @FXML
    private Button tasksButton, taskHistoryButton, reviewsButton, revenueDashboardButton;

    @FXML
    private VBox content;

    String vendorId = App.getData().getString("id");

    // Make tasksContainer an instance variable so that it is accessible from helper
    // methods.
    private ListView<Task> tasksContainer;

    Runners runnersDB = new Runners();
    Orders ordersDB = new Orders();

    @FXML
    private void initialize() {
        Navbar navbar = new Navbar("Runner", App.getData().getString("name"));
        HBox.setHgrow(navbar, Priority.ALWAYS);
        navbarContent.getChildren().add(navbar);
        System.out.println(App.getData().toString());

        // Initialize the tasks ListView for active tasks
        tasksContainer = new ListView<>();

        tasksContainer.setFocusModel(null);
        tasksContainer.setSelectionModel(null);

        tasksContainer.setCellFactory(lv -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContainer = new VBox(5);

                    Label taskIdLabel = new Label("Task ID: " + task.getTaskId());
                    String customerName = Customers.getCustomerName(task.getCustomerId());
                    Label customerIdLabel = new Label("Customer: " + customerName);

                    // Create a VBox for the items list
                    VBox itemsBox = new VBox(3);
                    for (Entry<String, Integer> entry : task.getItems().entrySet()) {
                        Label itemLabel = new Label(Items.getItemName(entry.getKey()) + " " + entry.getValue() + "x");
                        itemsBox.getChildren().add(itemLabel);
                    }

                    HBox buttonsContainer = new HBox(10);

                    Button acceptButton = new Button("Accept");
                    Button declineButton = new Button("Decline");
                    Button completeButton = new Button("Complete");

                    // When each button is pressed, update the DB, and then refresh the list
                    acceptButton.setOnAction(e -> {
                        runnersDB.acceptTask(vendorId, task.getTaskId());
                        updateTaskList();
                    });

                    declineButton.setOnAction(e -> {
                        runnersDB.declineTask(vendorId, task.getTaskId());
                        updateTaskList();
                    });

                    completeButton.setOnAction(e -> {
                        runnersDB.completeTask(vendorId, task.getTaskId());
                        updateTaskList();
                    });

                    // Display buttons depending on the task status
                    switch (task.getStatus()) {
                        case "wait_runner":
                            buttonsContainer.getChildren().addAll(acceptButton, declineButton);
                            break;
                        case "ongoing":
                            buttonsContainer.getChildren().add(completeButton);
                            break;
                    }

                    cellContainer.getChildren().addAll(taskIdLabel, customerIdLabel, itemsBox, buttonsContainer);

                    CellContainer container = new CellContainer(cellContainer);

                    setGraphic(container);
                }
            }
        });

        // Build the initial task list for active tasks
        updateTaskList();

        // Set up the event handler for task history button
        taskHistoryButton.setOnAction(e -> showTaskHistory());

        // Set up the event handler for tasks button to show active tasks
        tasksButton.setOnAction(e -> showActiveTasks());

        reviewsButton.setOnAction(e -> showCustomerReviews());

        revenueDashboardButton.setOnAction(e -> showRevenueDashboard());

        if (!tasksContainer.getItems().isEmpty()) {
            content.getChildren().clear();
            content.getChildren().add(tasksContainer);
        } else {
            Label noTasksLabel = new Label("No active tasks");

            content.getChildren().clear();
            content.getChildren().add(noTasksLabel);
        }

        // By default, add the tasks list to your content container
    }

    // Helper method to re-fetch and update the active tasks list
    private void updateTaskList() {
        ordersDB.refreshData();
        ObservableList<Task> tasks = FXCollections.observableArrayList();

        JSONObject runnerObject = runnersDB.getById(App.getData().getString("id"));

        // Loop through each task id available to the runner
        for (Object taskString : runnerObject.getJSONArray("tasks")) {
            String taskId = (String) taskString;

            JSONObject selectedOrder = ordersDB.getById(taskId);
            // Skip completed orders (they will be shown in task history)
            if (selectedOrder.getString("status").equals("completed")) {
                continue;
            }

            JSONArray orderItems = selectedOrder.getJSONArray("items");
            Map<String, Integer> itemCount = new HashMap<>();
            for (Object orderItem : orderItems) {
                String itemId = (String) orderItem;
                itemCount.put(itemId, itemCount.getOrDefault(itemId, 0) + 1);
            }

            tasks.add(new Task(taskId, selectedOrder.getString("customerId"), itemCount,
                    selectedOrder.getString("status")));
        }

        // Update the ListView with the new tasks list
        tasksContainer.setItems(tasks);
    }

    // Helper method to display a listview of completed tasks (the task history)
    private void showTaskHistory() {
        // Create a new ListView for the task history
        ListView<JSONObject> taskHistoryList = new ListView<>();
        ObservableList<JSONObject> tasksHistory = FXCollections.observableArrayList();

        JSONObject runnerObject = runnersDB.getById(App.getData().getString("id"));

        // Loop through the tasks key from the runner object
        for (Object taskObj : runnerObject.getJSONArray("tasks")) {
            String taskId = (String) taskObj;
            JSONObject order = ordersDB.getById(taskId);
            if (order.getString("status").equals("completed")) {
                // Optionally, store the task id within the order JSON for display purposes
                order.put("id", taskId);
                tasksHistory.add(order);
            }
        }

        taskHistoryList.setItems(tasksHistory);
        taskHistoryList.setFocusModel(null);
        taskHistoryList.setSelectionModel(null);
        taskHistoryList.setMouseTransparent(true);

        // Set a cell factory that builds a VBox for each completed task with id, date,
        // and customer name.
        taskHistoryList.setCellFactory(lv -> new ListCell<JSONObject>() {
            @Override
            protected void updateItem(JSONObject order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    // Assume order contains a "date" field; otherwise show "N/A"
                    Label dateLabel = new Label("Date: " + HumanDate.convert(order.getString("date")));
                    // Fetch the customer name from the Customers DB using customerId found in order
                    String customerName = Customers.getCustomerName(order.getString("customerId"));
                    Label customerLabel = new Label("Customer: " + customerName);

                    vbox.getChildren().addAll(dateLabel, customerLabel);

                    CellContainer container = new CellContainer(vbox);

                    setGraphic(container);
                }
            }
        });

        // Clear the existing content and add the task history list view
        content.getChildren().clear();
        content.getChildren().add(taskHistoryList);
    }

    // Helper method to show active tasks (original tasks list view)
    private void showActiveTasks() {
        // Refresh the active tasks list
        updateTaskList();

        if (!tasksContainer.getItems().isEmpty()) {
            content.getChildren().clear();
            content.getChildren().add(tasksContainer);
        } else {
            Label noTasksLabel = new Label("No active tasks");

            content.getChildren().clear();
            content.getChildren().add(noTasksLabel);
        }
    }

    private void showCustomerReviews() {
        Reviews reviewsDB = new Reviews();
        JSONArray vendorReviews = reviewsDB.getReviewsByTypeAndId(ReviewType.RUNNER, vendorId);

        ObservableList<Review> reviews = FXCollections.observableArrayList();

        for (Object element : vendorReviews) {
            JSONObject json = (JSONObject) element;
            reviews.add(new Review(json.getString("id"), json.getString("comment"), json.getInt("rating"),
                    json.getString("sender")));
        }

        ListView<Review> reviewList = new ListView<>(reviews);

        reviewList.setFocusModel(null);
        reviewList.setSelectionModel(null);
        reviewList.setMouseTransparent(true);

        reviewList.setCellFactory(lv -> new ListCell<Review>() {
            @Override
            protected void updateItem(Review order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    String customerName = Customers.getCustomerName(order.getSender());
                    Label customerNameLabel = new Label("User: " + customerName);

                    Label ratingLabel = new Label("Rating: " + order.getRating());
                    Label commentLabel = new Label("Comment: " + order.getComment());

                    vbox.getChildren().addAll(customerNameLabel, ratingLabel, commentLabel);

                    CellContainer container = new CellContainer(vbox);

                    setGraphic(container);
                }
            }
        });

        content.getChildren().clear();
        content.getChildren().add(reviewList);
    }

    private void showRevenueDashboard() {
        ordersDB.refreshData();
        JSONObject runnerObject = runnersDB.getById(App.getData().getString("id"));
        JSONArray tasksArray = runnerObject.getJSONArray("tasks");

        ObservableList<RevenueInfo> revenueInfo = FXCollections.observableArrayList();

        for (Object taskString : tasksArray) {
            String taskId = (String) taskString;

            JSONObject selectedOrder = ordersDB.getById(taskId);

            if (selectedOrder.getString("status").equals("completed")) {
                revenueInfo
                        .add(new RevenueInfo(taskId, selectedOrder.getString("date"),
                                selectedOrder.getInt("deliveryFee")));
            }
        }

        ListView<RevenueInfo> revenueList = new ListView<>(revenueInfo);

        revenueList.setCellFactory(lv -> new ListCell<RevenueInfo>() {
            @Override
            protected void updateItem(RevenueInfo order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    Label idLabel = new Label("Task ID: " + order.getOrderId());
                    Label dateLabel = new Label("Date: " + HumanDate.convert(order.getDate()));
                    Label revenueLabel = new Label("Revenue: " + order.getRevenue());

                    vbox.getChildren().addAll(idLabel, dateLabel, revenueLabel);
                    CellContainer container = new CellContainer(vbox);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(revenueList, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(revenueList);
    }
}
