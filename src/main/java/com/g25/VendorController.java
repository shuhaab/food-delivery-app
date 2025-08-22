package com.g25;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import com.g25.vendor.ItemPrice;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.g25.db.Orders;
import com.g25.db.Reviews;
import com.g25.db.Items;
import com.g25.customcomponent.CellContainer;
import com.g25.customcomponent.Navbar;
import com.g25.db.Customers;
import com.g25.db.Notifications;

class ProcessedOrder {
    private Map<String, Integer> items;
    private double totalPrice;
    private String orderId;
    private String orderType;
    private String customerId;

    public ProcessedOrder(Map<String, Integer> items, double totalPrice, String orderId, String orderType,
            String customerId) {
        this.items = items;
        this.totalPrice = totalPrice;
        this.orderId = orderId;
        this.orderType = orderType;
        this.customerId = customerId;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getCustomerId() {
        return customerId;
    }
}

public class VendorController {
    @FXML
    HBox navbarContent;

    @FXML
    Button itemsButton, ordersButton, orderHistoryButton, reviewsButton, revenueDashboardButton, notificationsButton;

    @FXML
    VBox content;

    final String vendorId = App.getData().getString("id");

    @FXML
    private void initialize() {
        Navbar navbar = new Navbar("Vendor", App.getData().getString("name"));
        HBox.setHgrow(navbar, Priority.ALWAYS);
        navbarContent.getChildren().add(navbar);

        itemsButton.setOnAction(e -> {
            displayItemsTable();
        });

        ordersButton.setOnAction(e -> {
            displayOrders();
        });

        orderHistoryButton.setOnAction(e -> {
            displayOrderHistory();
        });

        reviewsButton.setOnAction(e -> {
            displayReviews();
        });

        revenueDashboardButton.setOnAction(e -> {
            displayRevenueDashboard();
        });

        notificationsButton.setOnAction(e -> {
            displayNotifications();
        });

        displayItemsTable();
    }

    private void displayItemsTable() {
        String vendorId = App.getData().getString("id");
        Items itemsDB = new Items();
        // Build observable list from items database.
        ObservableList<ItemPrice> data = FXCollections.observableArrayList();
        JSONArray itemsArray = itemsDB.getAllVendorItems(vendorId);
        for (Object element : itemsArray) {
            JSONObject json = (JSONObject) element;
            // Assumes that your ItemPrice class has a constructor like:
            // ItemPrice(String id, String item, double price)
            if (json.getString("vendorId").equals(vendorId)) {
                data.add(new ItemPrice(
                        json.getString("id"),
                        json.getString("itemName"),
                        json.getDouble("price")));
            }
        }

        // Create the TableView and set it to be editable.
        TableView<ItemPrice> tableView = new TableView<>();
        tableView.setEditable(true);
        // Allow multiple selection for batch deletion.
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /* --- Item Name Column (with in-cell editing) --- */
        TableColumn<ItemPrice, String> nameColumn = new TableColumn<>("Item");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        nameColumn.setPrefWidth(150.0);
        // Use a TextFieldTableCell so that it will be directly editable.
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            ItemPrice item = event.getRowValue();
            item.setItem(event.getNewValue());
            // Immediately update the DB with the new value.
            updateItemInDB(item);
        });

        /* --- Price Column (with in-cell editing) --- */
        TableColumn<ItemPrice, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setPrefWidth(150.0);

        // Custom StringConverter to convert between Double and String with proper
        // formatting
        priceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Double>() {
            private final DecimalFormat formatter = new DecimalFormat("#,##0.00");

            @Override
            public String toString(Double value) {
                if (value == null) {
                    return "";
                }
                return formatter.format(value);
            }

            @Override
            public Double fromString(String value) {
                if (value == null || value.trim().isEmpty()) {
                    return 0.0;
                }
                try {
                    // Remove any currency symbols and commas before parsing
                    String cleanValue = value.replaceAll("[^\\d.-]", "");
                    return Double.parseDouble(cleanValue);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        }));

        priceColumn.setOnEditCommit(event -> {
            ItemPrice item = event.getRowValue();
            item.setPrice(event.getNewValue());
            updateItemInDB(item);
        });

        // Add the columns to the TableView.
        tableView.getColumns().addAll(nameColumn, priceColumn);
        tableView.setItems(data);

        /* --- "Add" Button for new item creation --- */
        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            // Create a pop-up dialog for adding a new item.
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add New Item");
            VBox dialogVBox = new VBox(10);
            dialogVBox.setPadding(new Insets(10));

            TextField nameField = new TextField();
            nameField.setPromptText("Item Name");

            TextField priceField = new TextField();
            priceField.setPromptText("Price");

            Button saveButton = new Button("Save");
            saveButton.setOnAction(ev -> {
                String name = nameField.getText().trim();
                double price = 0.0;
                try {
                    price = Double.parseDouble(priceField.getText().trim());
                } catch (NumberFormatException ex) {
                    // Optionally, show a warning alert here.
                    return;
                }
                // Create a new JSON object for the item.
                itemsDB.AddItem(name, price, vendorId);

                dialog.close();
            });

            dialogVBox.getChildren().addAll(
                    new Label("Item Name:"), nameField,
                    new Label("Price:"), priceField,
                    saveButton);
            Scene dialogScene = new Scene(dialogVBox, 300, 200);
            dialog.setScene(dialogScene);
            dialog.showAndWait();
        });

        /* --- "Delete Selected" Button for batch deletion --- */
        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            ObservableList<ItemPrice> selectedItems = tableView.getSelectionModel().getSelectedItems();
            if (selectedItems.isEmpty()) {
                return;
            }
            // Remove each selected item from the DB.
            JSONArray updatedArray = itemsDB.getDetachedData();
            // Iterate over a copy so we avoid concurrent modifications.
            List<ItemPrice> toRemove = new ArrayList<>(selectedItems);
            for (ItemPrice item : toRemove) {
                for (int i = 0; i < updatedArray.length(); i++) {
                    JSONObject json = updatedArray.getJSONObject(i);
                    if (json.getString("id").equals(item.getId())) {
                        updatedArray.remove(i);
                        break;
                    }
                }
            }
            itemsDB.setData(updatedArray);
            itemsDB.updateFileContent();
            data.removeAll(toRemove);
        });

        // Create a controls container for the Add and Delete buttons.
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().addAll(addButton, deleteButton);

        // Insert the controls and the table into the main content container.
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(controls, tableView);

        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(tableContainer);
    }

    /**
     * Update the given item in the items database.
     */
    private void updateItemInDB(ItemPrice updatedItem) {
        Items itemsDB = new Items();
        JSONArray itemsArray = itemsDB.getDetachedData();
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject json = itemsArray.getJSONObject(i);
            if (json.getString("id").equals(updatedItem.getId())) {
                json.put("itemName", updatedItem.getItem());
                json.put("price", updatedItem.getPrice());
                break;
            }
        }
        itemsDB.setData(itemsArray);
        itemsDB.updateFileContent();
    }

    private void displayOrders() {
        Orders ordersDB = new Orders();
        Notifications notificationsDB = new Notifications();

        String vendorId = App.getData().getString("id");
        JSONArray ordersForVendor = new JSONArray();

        for (Object element : ordersDB.getDetachedData()) {
            JSONObject json = (JSONObject) element;
            if (json.getString("vendorId").equals(vendorId)
                    && json.getString("status").equals("wait_vendor")) {
                ordersForVendor.put(json);
            }
        }

        // Process orders into a list of ProcessedOrder objects.
        List<ProcessedOrder> processedOrdersList = new ArrayList<>();
        for (Object element : ordersForVendor) {
            JSONObject json = (JSONObject) element;
            Map<String, Integer> items = new HashMap<>();
            for (Object itemEl : json.getJSONArray("items")) {
                String itemId = (String) itemEl;
                items.put(itemId, items.getOrDefault(itemId, 0) + 1);
            }
            ProcessedOrder processedOrder = new ProcessedOrder(items, json.getDouble("total"), json.getString("id"),
                    json.getString("orderType"), json.getString("customerId"));
            processedOrdersList.add(processedOrder);
        }

        // Create an observable list from our processed orders.
        ObservableList<ProcessedOrder> ordersObservableList = FXCollections.observableArrayList(processedOrdersList);

        // Create a ListView and set a custom cell factory.
        ListView<ProcessedOrder> ordersListView = new ListView<>(ordersObservableList);
        ordersListView.setCellFactory(listView -> new ListCell<ProcessedOrder>() {
            @Override
            protected void updateItem(ProcessedOrder processedOrder, boolean empty) {
                super.updateItem(processedOrder, empty);

                if (empty || processedOrder == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox orderRow = new VBox();
                    orderRow.setSpacing(10);
                    orderRow.setAlignment(Pos.CENTER_LEFT);

                    // Display each item for the order.
                    for (String itemId : processedOrder.getItems().keySet()) {
                        String itemName = Items.getItemName(itemId);
                        int quantity = processedOrder.getItems().get(itemId);
                        Label itemLabel = new Label(itemName + " " + quantity + "x");
                        orderRow.getChildren().add(itemLabel);
                    }

                    Label totalLabel = new Label("Total: " + processedOrder.getTotalPrice());
                    orderRow.getChildren().add(totalLabel);

                    // Container for the action buttons.
                    HBox buttonsContainer = new HBox();
                    buttonsContainer.setSpacing(10);

                    Button acceptButton = new Button("Accept");
                    acceptButton.setOnAction(e -> {
                        JSONArray ordersArray = ordersDB.getData();
                        for (Object element : ordersArray) {
                            JSONObject order = (JSONObject) element;
                            if (order.getString("id").equals(processedOrder.getOrderId())) {
                                switch (processedOrder.getOrderType()) {
                                    case "delivery":
                                        ordersDB.acceptOrder(processedOrder.getOrderId());
                                        break;
                                    default:
                                        order.put("status", "accepted");
                                        break;
                                }
                            }
                        }

                        ordersObservableList.remove(processedOrder);
                    });

                    Button declineButton = new Button("Decline");
                    declineButton.setOnAction(e -> {
                        // Update the database and remove the order from the observable list.
                        JSONArray ordersArray = ordersDB.getDetachedData();
                        for (Object element : ordersArray) {
                            JSONObject order = (JSONObject) element;
                            if (order.getString("id").equals(processedOrder.getOrderId())) {
                                order.put("status", "declined");
                                notificationsDB.addNotification("customer", processedOrder.getCustomerId(),
                                        "Your order has been declined by the vendor");
                                break;
                            }
                        }
                        ordersDB.setData(ordersArray);
                        ordersDB.updateFileContent();

                        ordersObservableList.remove(processedOrder);
                    });

                    buttonsContainer.getChildren().addAll(acceptButton, declineButton);
                    orderRow.getChildren().add(buttonsContainer);

                    CellContainer container = new CellContainer(orderRow);

                    setGraphic(container);
                }
            }
        });

        // Clear the content container and add our ListView.
        content.getChildren().clear();
        content.getChildren().add(ordersListView);
    }

    private void displayOrderHistory() {
        Orders ordersDB = new Orders();
        JSONArray ordersForVendor = new JSONArray();

        String vendorId = App.getData().getString("id");
        for (Object element : ordersDB.getDetachedData()) {
            JSONObject json = (JSONObject) element;
            if (json.getString("vendorId").equals(vendorId)) {
                ordersForVendor.put(json);
            }
        }

        // Create controls for filtering
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter by period:");
        ComboBox<String> periodComboBox = new ComboBox<>();
        periodComboBox.getItems().addAll("All", "Daily", "Monthly", "Quarterly", "Yearly");
        periodComboBox.setValue("All");
        Label dateSelectLabel = new Label("Select Date:");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        Button filterButton = new Button("Apply Filter");
        filterContainer.getChildren().addAll(filterLabel, periodComboBox, dateSelectLabel, datePicker, filterButton);

        // Create the ListView for orders; each cell will display an order's details.
        ObservableList<JSONObject> orderObservableList = FXCollections.observableArrayList();
        ListView<JSONObject> ordersListView = new ListView<>(orderObservableList);
        ordersListView.setCellFactory(listView -> new ListCell<JSONObject>() {
            @Override
            protected void updateItem(JSONObject order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox orderRow = new VBox();
                    orderRow.setSpacing(10);
                    orderRow.setAlignment(Pos.CENTER_LEFT);

                    // Build item count from order items.
                    Map<String, Integer> itemCount = new HashMap<>();
                    for (Object itemEl : order.getJSONArray("items")) {
                        String itemId = (String) itemEl;
                        itemCount.put(itemId, itemCount.getOrDefault(itemId, 0) + 1);
                    }
                    for (String itemId : itemCount.keySet()) {
                        String itemName = Items.getItemName(itemId);
                        int quantity = itemCount.get(itemId);
                        Label itemLabel = new Label(itemName + " " + quantity + "x");
                        orderRow.getChildren().add(itemLabel);
                    }

                    Label orderTypeLabel = new Label("Order Type: " + order.getString("orderType"));
                    Label statusLabel = new Label("Status: " + order.getString("status"));
                    Label dateLabel = new Label("Date: " + order.getString("date"));
                    Label totalLabel = new Label("Total: RM " + order.getDouble("total"));

                    orderRow.getChildren().addAll(orderTypeLabel, statusLabel, dateLabel, totalLabel);
                    // Wrap the row inside a CellContainer if required.
                    setGraphic(new CellContainer(orderRow));
                }
            }
        });

        // On filter button click, update the observable list
        filterButton.setOnAction(e -> {
            orderObservableList.clear();
            String filter = periodComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            // Date formatter used to parse stored dates.
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            for (Object element : ordersForVendor) {
                JSONObject json = (JSONObject) element;
                Date orderDateObj;
                try {
                    orderDateObj = dateFormat.parse(json.getString("date"));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    continue;
                }
                LocalDate orderDate = orderDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                boolean match = false;
                if (filter.equals("All")) {
                    match = true;
                } else if (filter.equals("Daily")) {
                    match = orderDate.isEqual(selectedDate);
                } else if (filter.equals("Monthly")) {
                    match = (orderDate.getYear() == selectedDate.getYear()
                            && orderDate.getMonth() == selectedDate.getMonth());
                } else if (filter.equals("Quarterly")) {
                    int orderQuarter = (orderDate.getMonthValue() - 1) / 3 + 1;
                    int selectedQuarter = (selectedDate.getMonthValue() - 1) / 3 + 1;
                    match = (orderDate.getYear() == selectedDate.getYear()
                            && orderQuarter == selectedQuarter);
                } else if (filter.equals("Yearly")) {
                    match = (orderDate.getYear() == selectedDate.getYear());
                }

                if (match) {
                    orderObservableList.add(json);
                }
            }
        });

        // Fire filter on load to display all orders.
        filterButton.fire();

        VBox.setVgrow(ordersListView, Priority.ALWAYS);

        // Combine filter controls and list view into a single container.
        VBox pageContainer = new VBox(20);
        pageContainer.getChildren().addAll(filterContainer, ordersListView);

        VBox.setVgrow(pageContainer, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(pageContainer);
    }

    private void displayReviews() {
        Reviews reviewsDB = new Reviews();
        Customers customersDB = new Customers();

        String vendorId = App.getData().getString("id");
        JSONArray reviewsForVendor = new JSONArray();

        for (Object element : reviewsDB.getDetachedData()) {
            JSONObject json = (JSONObject) element;
            if (json.getString("type").equals("vendor") && json.getString("vendorId").equals(vendorId)) {
                reviewsForVendor.put(json);
            }
        }

        // Create an observable list for reviews
        ObservableList<JSONObject> reviewsObservableList = FXCollections.observableArrayList();
        for (Object element : reviewsForVendor) {
            reviewsObservableList.add((JSONObject) element);
        }

        // Create ListView with a custom cell factory to display each review.
        ListView<JSONObject> reviewsListView = new ListView<>(reviewsObservableList);
        reviewsListView.setCellFactory(listView -> new ListCell<JSONObject>() {
            @Override
            protected void updateItem(JSONObject review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox reviewRow = new VBox();
                    reviewRow.setSpacing(10);
                    reviewRow.setAlignment(Pos.CENTER_LEFT);

                    String customerName = customersDB.getById(review.getString("sender")).getString("name");
                    Label customerNameLabel = new Label("User: " + customerName);
                    Label ratingLabel = new Label("Rating: " + review.getInt("rating"));
                    Label commentLabel = new Label("Comment: " + review.getString("comment"));

                    reviewRow.getChildren().addAll(customerNameLabel, ratingLabel, commentLabel);
                    setGraphic(new CellContainer(reviewRow));
                }
            }
        });

        VBox.setVgrow(reviewsListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(reviewsListView);
    }

    private void displayRevenueDashboard() {
        // Helper class to encapsulate aggregated revenue data.
        class RevenueAggregate {
            private double totalRevenue;
            private int orderCount;
            private double averageRevenue;

            public RevenueAggregate(double totalRevenue, int orderCount, double averageRevenue) {
                this.totalRevenue = totalRevenue;
                this.orderCount = orderCount;
                this.averageRevenue = averageRevenue;
            }

            public double getTotalRevenue() {
                return totalRevenue;
            }

            public int getOrderCount() {
                return orderCount;
            }

            public double getAverageRevenue() {
                return averageRevenue;
            }
        }

        Orders ordersDB = new Orders();
        String vendorId = App.getData().getString("id");

        // Filter only the completed orders for the vendor.
        JSONArray vendorOrders = new JSONArray();
        for (Object element : ordersDB.getDetachedData()) {
            JSONObject json = (JSONObject) element;
            if (json.getString("vendorId").equals(vendorId) && json.getString("status").equals("completed")) {
                vendorOrders.put(json);
            }
        }

        // Create filter controls.
        HBox filterContainer = new HBox(10);
        filterContainer.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter by period:");
        ComboBox<String> periodComboBox = new ComboBox<>();
        periodComboBox.getItems().addAll("All", "Daily", "Monthly", "Quarterly", "Yearly");
        periodComboBox.setValue("All");

        Label dateSelectLabel = new Label("Select Date:");
        DatePicker datePicker = new DatePicker(LocalDate.now());

        Button filterButton = new Button("Apply Filter");
        filterContainer.getChildren().addAll(filterLabel, periodComboBox, dateSelectLabel, datePicker, filterButton);

        // Create an observable list to hold the aggregated revenue result.
        ObservableList<RevenueAggregate> revenueAggregates = FXCollections.observableArrayList();
        ListView<RevenueAggregate> dashboardListView = new ListView<>(revenueAggregates);
        dashboardListView.setCellFactory(listView -> new ListCell<RevenueAggregate>() {
            @Override
            protected void updateItem(RevenueAggregate aggregate, boolean empty) {
                super.updateItem(aggregate, empty);
                if (empty || aggregate == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox aggregateBox = new VBox(10);
                    aggregateBox.setAlignment(Pos.CENTER_LEFT);
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    Label totalRevenueLabel = new Label("Total Revenue: RM " + df.format(aggregate.getTotalRevenue()));
                    Label orderCountLabel = new Label("Number of Orders: " + aggregate.getOrderCount());
                    Label averageRevenueLabel = new Label(
                            "Average Revenue per Order: RM " + df.format(aggregate.getAverageRevenue()));

                    aggregateBox.getChildren().addAll(totalRevenueLabel, orderCountLabel, averageRevenueLabel);
                    setGraphic(new CellContainer(aggregateBox));
                }
            }
        });

        // Date formatter to parse stored order dates.
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        // When the filter button is pressed, aggregate revenue data for orders matching
        // the filter.
        filterButton.setOnAction(e -> {
            revenueAggregates.clear();
            String filter = periodComboBox.getValue();
            LocalDate selectedDate = datePicker.getValue();
            double totalRevenue = 0;
            int orderCount = 0;

            for (Object element : vendorOrders) {
                JSONObject order = (JSONObject) element;
                Date orderDateObj;
                try {
                    orderDateObj = dateFormat.parse(order.getString("date"));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    continue;
                }
                LocalDate orderDate = orderDateObj.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                boolean match = false;
                if (filter.equals("All")) {
                    match = true;
                } else if (filter.equals("Daily")) {
                    match = orderDate.isEqual(selectedDate);
                } else if (filter.equals("Monthly")) {
                    match = (orderDate.getYear() == selectedDate.getYear()
                            && orderDate.getMonth() == selectedDate.getMonth());
                } else if (filter.equals("Quarterly")) {
                    int orderQuarter = (orderDate.getMonthValue() - 1) / 3 + 1;
                    int selectedQuarter = (selectedDate.getMonthValue() - 1) / 3 + 1;
                    match = (orderDate.getYear() == selectedDate.getYear() && orderQuarter == selectedQuarter);
                } else if (filter.equals("Yearly")) {
                    match = (orderDate.getYear() == selectedDate.getYear());
                }

                if (match) {
                    totalRevenue += order.getDouble("total"); // Use the total as revenue.
                    orderCount++;
                }
            }

            double averageRevenue = orderCount > 0 ? totalRevenue / orderCount : 0;
            RevenueAggregate aggregate = new RevenueAggregate(totalRevenue, orderCount, averageRevenue);
            revenueAggregates.add(aggregate);
        });

        // Fire filter on load to display results initially.
        filterButton.fire();

        VBox dashboardContainer = new VBox(20);
        dashboardContainer.getChildren().addAll(filterContainer, dashboardListView);

        content.getChildren().clear();
        content.getChildren().add(dashboardContainer);
    }

    public void displayNotifications() {
        Notifications notificationsDB = new Notifications();
        System.out.println(vendorId);
        JSONArray vendorNotifications = notificationsDB.getVendorNotifications(vendorId);

        ObservableList<NotificationInfo> notificationList = FXCollections.observableArrayList();

        for (Object element : vendorNotifications) {
            JSONObject json = (JSONObject) element;
            notificationList.add(new NotificationInfo(json.getString("message")));
        }

        ListView<NotificationInfo> notificationListView = new ListView<>(notificationList);
        notificationListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NotificationInfo notificationInfo, boolean empty) {
                super.updateItem(notificationInfo, empty);
                if (empty || notificationInfo == null) {
                    setText(null);
                    setGraphic(null);
                    setMouseTransparent(true);
                } else {
                    setMouseTransparent(false);
                    Label messageLabel = new Label(notificationInfo.getMessage());

                    CellContainer container = new CellContainer(messageLabel);

                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(notificationListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(notificationListView);
    }
}