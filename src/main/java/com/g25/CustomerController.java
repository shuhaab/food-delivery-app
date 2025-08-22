package com.g25;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.g25.customcomponent.CellContainer;
import com.g25.customcomponent.Navbar;

import com.g25.db.Complaints;
import com.g25.db.Customers;
import com.g25.db.Items;
import com.g25.db.Notifications;
import com.g25.db.Orders;
import com.g25.db.Requests;
import com.g25.db.Reviews;
import com.g25.db.Vendors;
import com.g25.db.Orders.OrderType;
import com.g25.Shared.ComplaintInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

class OrderInfo {
    private String id;
    private String date;
    private String type;
    private String status;
    private String vendorId;
    private String runnerId;
    private List<String> items;
    private Double total;

    public OrderInfo(String id, String date, String type, String status, String vendorId, List<String> items,
            Double total, String runnerId) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.status = status;
        this.vendorId = vendorId;
        this.items = items;
        this.total = total;
        this.runnerId = runnerId;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getVendorId() {
        return vendorId;
    }

    public List<String> getItems() {
        return items;
    }

    public Double getTotal() {
        return total;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public OrderType getType() {
        switch (type) {
            case "delivery":
                return OrderType.DELIVERY;
            case "dinein":
                return OrderType.DINEIN;
            case "takeaway":
                return OrderType.TAKEAWAY;
        }
        return null;
    }
}

class ReviewInfo {
    private String id;
    private String type;
    private Integer rating;
    private String comment;
    private String reviewedEntityId;

    public ReviewInfo(String id, String type, Integer rating, String comment, String reviewedEntityId) {
        this.id = id;
        this.type = type;
        this.rating = rating;
        this.comment = comment;
        this.reviewedEntityId = reviewedEntityId;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewedEntityId() {
        return reviewedEntityId;
    }
}

class VendorInfo {
    private String id;
    private String name;
    private String address;

    public VendorInfo(String id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}

class ItemInfo {
    private String id;
    private String name;
    private Double price;

    public ItemInfo(String id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }
}

class RequestInfo {
    private String id;
    private String date;
    private String status;
    private Double topupValue;

    public RequestInfo(String id, String date, String status, Double topupValue) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.topupValue = topupValue;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public Double getTopupValue() {
        return topupValue;
    }
}

class NotificationInfo {
    private String message;

    public NotificationInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

class CartItem {
    private String itemId;
    private int quantity;
    private double price;

    public CartItem(String itemId, int quantity, double price) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

class Cart {
    private String vendorId;
    private ObservableList<CartItem> items;

    public Cart() {
        this.items = FXCollections.observableArrayList();
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }
}

public class CustomerController {

    @FXML
    private HBox navbarContent;

    @FXML
    private Label creditsLabel;

    @FXML
    private Button menuButton, reviewsButton, ordersButton, transactionsButton, notificationsButton, complaintsButton,
            cartButton, topupButton;

    @FXML
    private VBox content;

    String customerId = App.getData().getString("id");

    Customers customersDB = new Customers();
    Vendors vendorsDB = new Vendors();
    Orders ordersDB = new Orders();
    Requests requestsDB = new Requests();
    Reviews reviewsDB = new Reviews();
    Items itemsDB = new Items();
    Complaints complaintsDB = new Complaints();

    Cart cart = new Cart();

    private void setActiveButton(Button selected) {
        Button[] navButtons = {
                menuButton,
                reviewsButton,
                ordersButton,
                transactionsButton,
                notificationsButton,
                complaintsButton,
                cartButton
        };

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active-button");
        }

        if (!selected.getStyleClass().contains("active-button")) {
            selected.getStyleClass().add("active-button");
        }
    }

    private Dialog<Double> showTopupDialog() {
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Topup value");
        dialog.setHeaderText("How much credit do you want to topup?");

        // Set button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create form components
        TextField valueField = new TextField();

        // Add input restrictions
        TextFormatter<String> textFormatter = new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[+-]?\\d*\\.?\\d*")) { // Allow numbers and decimal points
                return change;
            }
            return null;
        });
        valueField.setTextFormatter(textFormatter);

        // Get OK button and disable it initially
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Add validation listener
        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    Double.parseDouble(newValue);
                    okButton.setDisable(false);
                } else {
                    okButton.setDisable(true);
                }
            } catch (NumberFormatException e) {
                okButton.setDisable(true);
            }
        });

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Topup value:"), 0, 0);
        grid.add(valueField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        // Result converter with error handling
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    return Double.parseDouble(valueField.getText());
                } catch (NumberFormatException e) {
                    // This shouldn't happen due to validation, but added for safety
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    private Dialog<String[]> showReviewDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Review");
        dialog.setHeaderText("What do you think about this order?");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label ratingLabel = new Label("Rating:");
        Label commentLabel = new Label("Comment:");

        ComboBox<String> ratingDropdown = new ComboBox<>();
        ratingDropdown.getItems().addAll("1", "2", "3", "4", "5");
        ratingDropdown.setValue("5");

        TextField commentField = new TextField();

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        grid.add(ratingLabel, 0, 0);
        grid.add(ratingDropdown, 1, 0);
        grid.add(commentLabel, 0, 1);
        grid.add(commentField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String[] fieldData = new String[] {
                        ratingDropdown.getValue().toString(),
                        commentField.getText()
                };
                return fieldData;
            }
            return null;
        });

        return dialog;
    }

    private Dialog<String[]> showComplaintDialog() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Complaint");
        dialog.setHeaderText("What do you want to complain about?");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label titleLabel = new Label("Title:");
        Label descriptionLabel = new Label("Description:");

        TextField titleField = new TextField();
        TextField descriptionField = new TextField();

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        grid.add(titleLabel, 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(descriptionLabel, 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String[] fieldData = new String[] {
                        titleField.getText(),
                        descriptionField.getText()
                };
                return fieldData;
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void initialize() {
        Navbar navbar = new Navbar("Customer", App.getData().getString("name"));
        HBox.setHgrow(navbar, Priority.ALWAYS);
        navbarContent.getChildren().add(navbar);

        creditsLabel.setText(String.valueOf(customersDB.getById(customerId).getDouble("credits")));

        topupButton.setOnAction(e -> {
            requestsDB.refreshData();
            Dialog<Double> formDialog = showTopupDialog();
            formDialog.showAndWait().ifPresent(result -> {
                requestsDB.addRequest(customerId, result);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Topup successful");
                alert.setHeaderText("Your topup request has been sent to the admin");
                alert.showAndWait();
            });
        });

        menuButton.setOnAction(e -> {
            setActiveButton(menuButton);
            showMenu();
        });

        reviewsButton.setOnAction(e -> {
            setActiveButton(reviewsButton);
            showReviews();
        });

        ordersButton.setOnAction(e -> {
            setActiveButton(ordersButton);
            showOrderHistory();
        });

        transactionsButton.setOnAction(e -> {
            setActiveButton(transactionsButton);
            showTransactionHistory();
        });

        complaintsButton.setOnAction(e -> {
            setActiveButton(complaintsButton);
            showComplaints();
        });

        notificationsButton.setOnAction(e -> {
            setActiveButton(notificationsButton);
            showNotifications();
        });

        cartButton.setOnAction(e -> {
            setActiveButton(cartButton);
            showCart();
        });

        showMenu();
        setActiveButton(menuButton);
    }

    private void showMenu() {
        ObservableList<VendorInfo> vendors = FXCollections.observableArrayList();

        JSONArray vendorsData = vendorsDB.getDetachedData();
        for (Object element : vendorsData) {
            JSONObject vendorObject = (JSONObject) element;
            String id = vendorObject.getString("id");
            String name = vendorObject.getString("name");
            String address = vendorObject.getString("address");
            vendors.add(new VendorInfo(id, name, address));
        }

        ListView<VendorInfo> vendorsList = new ListView<>(vendors);

        vendorsList.setCellFactory(lv -> new ListCell<VendorInfo>() {
            @Override
            protected void updateItem(VendorInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContainer = new VBox(10);

                    Label vendorNameLabel = new Label("Vendor Name: " + item.getName());
                    Label vendorAddressLabel = new Label("Vendor Address: " + item.getAddress());

                    Button viewVendorMenuButton = new Button("View vendor menu");

                    viewVendorMenuButton.setOnAction(e -> showVendorMenu(item.getId()));

                    cellContainer.getChildren().addAll(vendorNameLabel, vendorAddressLabel, viewVendorMenuButton);

                    CellContainer container = new CellContainer(cellContainer);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(vendorsList, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(vendorsList);
    }

    private void showVendorMenu(String vendorId) {
        JSONArray vendorItems = vendorsDB.getItems(vendorId);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            showMenu();
        });

        ObservableList<ItemInfo> items = FXCollections.observableArrayList();

        for (Object element : vendorItems) {
            JSONObject itemObject = (JSONObject) element;

            String itemId = itemObject.getString("id");
            String itemName = itemObject.getString("itemName");
            Double itemPrice = itemObject.getDouble("price");

            items.add(new ItemInfo(itemId, itemName, itemPrice));
        }

        ListView<ItemInfo> itemsList = new ListView<>(items);

        itemsList.setCellFactory(lv -> new ListCell<ItemInfo>() {
            @Override
            protected void updateItem(ItemInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox cellContainer = new VBox(10);

                    Label itemNameLabel = new Label("Item Name: " + item.getName());
                    Label itemPriceLabel = new Label("Item Price: " + item.getPrice());

                    int initInCartCount = cart.getItems().stream()
                            .filter(i -> i.getItemId().equals(item.getId()))
                            .mapToInt(CartItem::getQuantity)
                            .sum();

                    Label itemAlreadyIncartLabel = new Label("Item already in cart: " + initInCartCount);
                    itemAlreadyIncartLabel.setVisible(initInCartCount > 0);

                    HBox quantityButtons = new HBox();

                    Button addToCartButton = new Button(initInCartCount > 0 ? "+" : "Add to cart");

                    Button minusButton = new Button("-");
                    minusButton.setVisible(initInCartCount > 0);

                    addToCartButton.setOnAction(e -> {
                        if (cart.getVendorId() != null && !cart.getVendorId().equals(vendorId)) {
                            Alert alert = new Alert(AlertType.CONFIRMATION);
                            alert.setHeaderText("Change vendor?");
                            alert.setContentText(
                                    "You can only order from a single vendor. Do you want to reset the cart?");

                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    cart.getItems().clear();
                                } else {
                                    return;
                                }
                            });
                        }

                        cart.setVendorId(vendorId);
                        ObservableList<CartItem> cartItems = cart.getItems();
                        int updatedCount = 0;
                        for (CartItem cartItem : cartItems) {
                            if (cartItem.getItemId().equals(item.getId())) {
                                cartItem.setQuantity(cartItem.getQuantity() + 1);
                                updatedCount = cartItem.getQuantity();
                                break;
                            }
                        }
                        if (updatedCount == 0) {
                            cartItems.add(new CartItem(item.getId(), 1, item.getPrice()));
                            updatedCount = 1;
                        }

                        addToCartButton.setText(updatedCount > 0 ? "+" : "Add to cart");

                        itemAlreadyIncartLabel.setText("Item already in cart: " + updatedCount);
                        itemAlreadyIncartLabel.setVisible(updatedCount > 0);
                        minusButton.setVisible(updatedCount > 0);
                    });

                    minusButton.setOnAction(e -> {
                        ObservableList<CartItem> cartItems = cart.getItems();
                        int updatedCount = 0;
                        for (CartItem cartItem : cartItems) {
                            if (cartItem.getItemId().equals(item.getId())) {
                                if (cartItem.getQuantity() - 1 == 0) {
                                    cartItems.remove(cartItem);
                                    updatedCount = 0;
                                } else {
                                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                                    updatedCount = cartItem.getQuantity();
                                }
                                break;
                            }
                        }

                        addToCartButton.setText(updatedCount > 0 ? "+" : "Add to cart");

                        itemAlreadyIncartLabel.setText("Item already in cart: " + updatedCount);
                        itemAlreadyIncartLabel.setVisible(updatedCount > 0);
                        minusButton.setVisible(updatedCount > 0);
                    });

                    quantityButtons.getChildren().addAll(addToCartButton, minusButton);

                    cellContainer.getChildren().addAll(itemNameLabel, itemPriceLabel, itemAlreadyIncartLabel,
                            quantityButtons);

                    CellContainer container = new CellContainer(cellContainer);

                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(itemsList, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().addAll(backButton, itemsList);
    }

    private void showReviews() {
        Reviews reviewsDB = new Reviews();
        JSONArray reviews = reviewsDB.getSenderReviews(customerId);

        ObservableList<ReviewInfo> reviewList = FXCollections.observableArrayList();

        for (Object element : reviews) {
            JSONObject review = (JSONObject) element;
            reviewList.add(new ReviewInfo(review.getString("id"), review.getString("type"), review.getInt("rating"),
                    review.getString("comment"), review.getString(review.getString("type") + "Id")));
        }

        ListView<ReviewInfo> reviewListView = new ListView<>(reviewList);

        reviewListView.setCellFactory(lv -> new ListCell<ReviewInfo>() {
            @Override
            protected void updateItem(ReviewInfo review, boolean empty) {
                super.updateItem(review, empty);
                if (empty || review == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    Label idLabel = new Label("Review ID: " + review.getId());
                    Label typeLabel = new Label("Type: " + review.getType());
                    Label ratingLabel = new Label("Rating: " + review.getRating());
                    Label commentLabel = new Label("Your comment: " + review.getComment());
                    Label reviewedEntityIdLabel = new Label("Reviewed Entity ID: " + review.getReviewedEntityId());

                    vbox.getChildren().addAll(idLabel, typeLabel, ratingLabel, commentLabel, reviewedEntityIdLabel);

                    CellContainer container = new CellContainer(vbox);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(reviewListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(reviewListView);
    }

    public void showOrderHistory() {
        ordersDB.refreshData();
        reviewsDB.refreshData();
        JSONArray orders = ordersDB.getCustomerOrders(customerId);

        ObservableList<OrderInfo> orderList = FXCollections.observableArrayList();

        for (Object element : orders) {
            JSONObject order = (JSONObject) element;

            List<String> itemIds = new ArrayList<>();

            String orderId = order.getString("id");
            String orderDate = order.getString("date");
            String orderType = order.getString("orderType");
            String orderStatus = order.getString("status");
            String vendorId = order.getString("vendorId");

            for (Object itemString : order.getJSONArray("items")) {
                String itemId = (String) itemString;
                itemIds.add(itemId);
            }

            String runnerIdStr = "";

            try {
                runnerIdStr = order.getString("runnerId");
            } catch (JSONException e) {
                runnerIdStr = "";
            }

            orderList.add(new OrderInfo(orderId, orderDate, orderType, orderStatus, vendorId, itemIds,
                    order.getDouble("total") + order.getDouble("deliveryFee"), runnerIdStr));
        }

        ListView<OrderInfo> ordersContainer = new ListView<>(orderList);

        ordersContainer.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(OrderInfo orderInfo, boolean empty) {
                super.updateItem(orderInfo, empty);
                if (empty || orderInfo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    HBox itemsContainer = new HBox();
                    Label itemsLabel = new Label("Items: ");
                    itemsContainer.getChildren().add(itemsLabel);

                    Button reorderButton = new Button("Reorder");
                    reorderButton.setVisible(
                            orderInfo.getStatus().equals("completed") || orderInfo.getStatus().equals("declined"));
                    reorderButton.setManaged(
                            orderInfo.getStatus().equals("completed") || orderInfo.getStatus().equals("declined"));

                    reorderButton.setOnAction(e -> {
                        boolean hasRemovedItem = false;

                        for (String itemId : orderInfo.getItems()) {
                            JSONObject selectedItem = itemsDB.getById(itemId);
                            if (selectedItem.getBoolean("removed")) {
                                hasRemovedItem = true;
                                break;
                            }
                        }

                        if (hasRemovedItem) {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Reorder failed");
                            alert.setHeaderText(
                                    "This order contains an item that has been removed from the system!");
                            alert.showAndWait();
                        } else {
                            ordersDB.createNewOrder(orderInfo.getVendorId(), customerId, orderInfo.getType(),
                                    orderInfo.getItems(), orderInfo.getTotal());

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Reorder sucessful");
                            alert.setHeaderText("The order has been successfully reordered");
                            alert.showAndWait();
                        }
                    });

                    Button reviewRunnerButton = new Button("Review runner");
                    reviewRunnerButton.setManaged(orderInfo.getStatus().equals("completed"));
                    if (orderInfo.getRunnerId() == "") {
                        reviewRunnerButton.setManaged(false);
                    } else {
                        // if the review of the runer exists, disable the button
                        for (Object element : reviewsDB.getSenderReviews(customerId)) {
                            JSONObject review = (JSONObject) element;
                            if (review.getString("type").equals("runner")) {
                                if (review.getString("runnerId").equals(orderInfo.getRunnerId())) {
                                    reviewRunnerButton.setText("Runner reviewed");
                                    reviewRunnerButton.setDisable(true);
                                    break;
                                }
                            }
                        }
                    }
                    reviewRunnerButton.setOnAction(e -> {
                        Dialog<String[]> reviewDialog = showReviewDialog();
                        reviewDialog.showAndWait().ifPresent(result -> {
                            int rating = Integer.parseInt(result[0]);
                            String comment = result[1];

                            reviewsDB.addReview(Reviews.ReviewType.RUNNER, customerId, rating, comment,
                                    orderInfo.getRunnerId());

                            getListView().refresh();
                        });
                    });

                    Button reviewVendorButton = new Button("Review vendor");
                    reviewVendorButton.setManaged(orderInfo.getStatus().equals("completed"));
                    // if the review of the vendor is exists, disable the button
                    for (Object element : reviewsDB.getSenderReviews(customerId)) {
                        JSONObject review = (JSONObject) element;
                        if (review.getString("type").equals("vendor")) {
                            if (review.getString("vendorId").equals(orderInfo.getVendorId())) {
                                reviewVendorButton.setText("Vendor reviewed");
                                reviewVendorButton.setDisable(true);
                                break;
                            }
                        }
                    }
                    reviewVendorButton.setOnAction(e -> {
                        Dialog<String[]> reviewDialog = showReviewDialog();
                        reviewDialog.showAndWait().ifPresent(result -> {
                            int rating = Integer.parseInt(result[0]);
                            String comment = result[1];

                            reviewsDB.addReview(Reviews.ReviewType.VENDOR, customerId, rating, comment,
                                    orderInfo.getVendorId());

                            getListView().refresh();
                        });
                    });

                    Button complaintButton = new Button("File a complaint");
                    // if complaint for this order exists, disable the button
                    for (Object element : complaintsDB.getCustomerComplaints(customerId)) {
                        JSONObject complaint = (JSONObject) element;
                        if (complaint.getString("orderId").equals(orderInfo.getId())) {
                            complaintButton.setDisable(true);
                            break;
                        }
                    }

                    complaintButton.setOnAction(e -> {
                        Dialog<String[]> complaintDialog = showComplaintDialog();
                        complaintDialog.showAndWait().ifPresent(result -> {
                            complaintsDB.addComplaint(customerId, result[0], result[1], orderInfo.getId());

                            getListView().refresh();
                        });
                    });

                    JSONArray reviews = reviewsDB.getSenderReviews(customerId);
                    boolean foundOrderReview = false;

                    for (Object element : reviews) {
                        JSONObject review = (JSONObject) element;

                        if (review.getString("type").equals("order")
                                && review.getString("orderId").equals(orderInfo.getId())) {
                            foundOrderReview = true;
                            break;
                        }
                    }

                    Button reviewButton = new Button(foundOrderReview ? "Order reviewed" : "Review order");
                    reviewButton.setDisable(foundOrderReview);
                    reviewButton.setManaged(orderInfo.getStatus().equals("completed"));
                    reviewButton.setOnAction(e -> {
                        Dialog<String[]> reviewDialog = showReviewDialog();
                        reviewDialog.showAndWait().ifPresent(result -> {
                            int rating = Integer.parseInt(result[0]);
                            String comment = result[1];

                            reviewsDB.addReview(Reviews.ReviewType.ORDER, customerId, rating, comment,
                                    orderInfo.getId());

                            getListView().refresh();
                        });
                    });

                    Map<String, Integer> itemCount = new HashMap<>();
                    for (Object itemEl : orderInfo.getItems()) {
                        String itemId = (String) itemEl;
                        itemCount.put(itemId, itemCount.getOrDefault(itemId, 0) + 1);
                    }

                    for (String itemName : itemCount.keySet()) {
                        int quantity = itemCount.get(itemName);
                        Label itemLabel = new Label(Items.getItemName(itemName) + " " + quantity + "x");
                        itemsContainer.getChildren().add(itemLabel);
                    }

                    Label dateLabel = new Label("Date: " + orderInfo.getDate());
                    Label statusLabel = new Label("Status: " + orderInfo.getStatus());
                    Label vendorIdLabel = new Label("Vendor ID: " + orderInfo.getVendorId());
                    Label totalLabel = new Label("Total: " + orderInfo.getTotal());
                    Label typeLabel = new Label("Type: " + orderInfo.getType());

                    HBox buttonsContainer = new HBox(10, reorderButton, reviewButton, reviewRunnerButton,
                            reviewVendorButton,
                            complaintButton);

                    vbox.getChildren().addAll(dateLabel, typeLabel, statusLabel, vendorIdLabel, itemsContainer,
                            totalLabel, buttonsContainer);

                    CellContainer container = new CellContainer(vbox);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(ordersContainer, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(ordersContainer);
    }

    public void showComplaints() {
        Complaints complaintsDB = new Complaints();
        JSONArray complaints = complaintsDB.getCustomerComplaints(customerId);

        ObservableList<ComplaintInfo> complaintList = FXCollections.observableArrayList();

        for (Object element : complaints) {
            JSONObject complaint = (JSONObject) element;
            complaintList.add(new ComplaintInfo(complaint.getString("orderId"), complaint.getString("complaintTitle"),
                    complaint.getString("description"), complaint.getJSONArray("replies")));
        }

        ListView<ComplaintInfo> complaintListView = new ListView<>(complaintList);

        complaintListView.setCellFactory(lv -> new ListCell<ComplaintInfo>() {
            @Override
            protected void updateItem(ComplaintInfo complaint, boolean empty) {
                super.updateItem(complaint, empty);
                if (empty || complaint == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    Label orderIdLabel = new Label("Order ID: " + complaint.getOrderId());
                    Label titleLabel = new Label("Title: " + complaint.getTitle());
                    Label descriptionLabel = new Label("Description: " + complaint.getDescription());

                    vbox.getChildren().addAll(orderIdLabel, titleLabel, descriptionLabel);

                    CellContainer container = new CellContainer(vbox);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(complaintListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(complaintListView);
    }

    public void showTransactionHistory() {
        JSONArray requests = Requests.getCustomerRequests(customerId);

        ObservableList<RequestInfo> requestList = FXCollections.observableArrayList();
        for (Object element : requests) {
            JSONObject request = (JSONObject) element;
            requestList.add(new RequestInfo(request.getString("id"), request.getString("date"),
                    request.getString("status"), request.getDouble("topupValue")));
        }

        ListView<RequestInfo> requestListView = new ListView<>(requestList);
        requestListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RequestInfo requestInfo, boolean empty) {
                super.updateItem(requestInfo, empty);
                if (empty || requestInfo == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox transactionsContainer = new VBox();

                    Label dateLabel = new Label(requestInfo.getDate());
                    Label statusLabel = new Label(requestInfo.getStatus());
                    Label topupValueLabel = new Label(String.valueOf(requestInfo.getTopupValue()));

                    transactionsContainer.getChildren().addAll(dateLabel, statusLabel, topupValueLabel);

                    CellContainer container = new CellContainer(transactionsContainer);

                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(requestListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(requestListView);
    }

    public void showNotifications() {
        Notifications notificationsDB = new Notifications();
        JSONArray customerNotifications = notificationsDB.getCustomerNotifications(customerId);

        ObservableList<NotificationInfo> notificationList = FXCollections.observableArrayList();

        for (Object element : customerNotifications) {
            JSONObject notification = (JSONObject) element;
            notificationList.add(new NotificationInfo(notification.getString("message")));
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
                    container.setAlignment(Pos.TOP_LEFT);

                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(notificationListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(notificationListView);
    }

    public void showCart() {
        ObservableList<CartItem> items = cart.getItems();

        ListView<CartItem> cartList = new ListView<>(FXCollections.observableList(items));

        Label totalItemPriceLabel = new Label();
        Label deliveryFeeLabel = new Label();
        Label totalPriceLabel = new Label();

        ComboBox<String> orderTypeComboBox = new ComboBox<>();
        orderTypeComboBox.getItems().addAll("Delivery", "Dine-in", "Takeaway");
        orderTypeComboBox.setValue("Delivery");

        Button orderButton = new Button("Order");

        Runnable update = () -> {
            double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
            totalItemPriceLabel.setText("Total item price: " + total);
            deliveryFeeLabel.setText("Delivery fee: " + total * 0.1);
            totalPriceLabel.setText("Total: " + (total + total * 0.1));

            if (items.isEmpty()) {
                orderButton.setDisable(true);
            }
        };

        update.run();

        orderButton.setOnAction(e -> {
            double total = items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();

            OrderType selectedOrderType = null;

            switch (orderTypeComboBox.getValue()) {
                case "Delivery":
                    selectedOrderType = OrderType.DELIVERY;
                    break;
                case "Dine-in":
                    selectedOrderType = OrderType.DINEIN;
                    break;
                case "Takeaway":
                    selectedOrderType = OrderType.TAKEAWAY;
                    break;
            }

            List<String> itemIds = new ArrayList<>();

            for (CartItem item : items) {
                for (int i = 0; i < item.getQuantity(); i++) {
                    itemIds.add(item.getItemId());
                }
            }

            double currentCredits = customersDB.getById(customerId).getDouble("credits");
            if (currentCredits - (total + total * 0.1) < 0) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Not enough credits");
                alert.setHeaderText("You don't have enough credits to pay for this order");
                alert.showAndWait();
            } else {
                ordersDB.createNewOrder(cart.getVendorId(), customerId, selectedOrderType,
                        itemIds, total);
                customersDB.refreshData();

                creditsLabel.setText("Credits: " + customersDB.getById(customerId).getDouble("credits"));

                cart.getItems().clear();

                update.run();
                cartList.refresh();
            }

        });

        cartList.setCellFactory(lv -> new ListCell<CartItem>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label itemNameLabel = new Label("Item Name: " + Items.getItemName(item.getItemId()));
                    Label itemPriceLabel = new Label("Item Price: " + item.getPrice());
                    Label quantityLabel = new Label("Quantity: " + item.getQuantity());

                    Button removeFromCartButton = new Button("Remove from cart");
                    removeFromCartButton.setOnAction(e -> {
                        items.remove(item);

                        if (cart.getItems().size() == 0) {
                            cart.setVendorId(null);
                        }

                        update.run();
                        getListView().refresh();
                    });

                    Button plusButton = new Button("+");
                    plusButton.setOnAction(e -> {
                        item.setQuantity(item.getQuantity() + 1);
                        update.run();
                        getListView().refresh();
                    });

                    Button minusButton = new Button("-");
                    minusButton.setOnAction(e -> {
                        if (item.getQuantity() > 0) {
                            item.setQuantity(item.getQuantity() - 1);
                            if (item.getQuantity() == 0) {
                                items.remove(item);
                            }
                            update.run();
                            getListView().refresh();
                        }
                    });

                    HBox buttonsContainer = new HBox(10, plusButton, minusButton, removeFromCartButton);
                    VBox cellContainer = new VBox(10, itemNameLabel, itemPriceLabel, quantityLabel, buttonsContainer);

                    CellContainer container = new CellContainer(cellContainer);

                    setGraphic(container);
                }
            }
        });

        content.getChildren().clear();
        content.getChildren().addAll(cartList, totalItemPriceLabel, deliveryFeeLabel, totalPriceLabel,
                orderTypeComboBox, orderButton);
    }
}