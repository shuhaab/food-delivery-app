package com.g25;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.g25.Icons.TrashIcon;
import com.g25.customcomponent.CellContainer;
import com.g25.customcomponent.Navbar;
import com.g25.db.Complaints;
import com.g25.db.Customers;
import com.g25.db.Items;
import com.g25.db.Managers;
import com.g25.db.Orders;
import com.g25.db.Runners;
import com.g25.db.Vendors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import com.g25.Shared.ComplaintInfo;
import com.g25.Utils.HumanDate;

class ComplaintInfoExtra extends ComplaintInfo {
    private String id;
    private String status;
    private String customerId;
    private String date;

    public ComplaintInfoExtra(String orderId, String title, String description,
            JSONArray replies, String status, String id, String customerId, String date) {
        super(orderId, title, description, replies);
        this.status = status;
        this.id = id;
        this.customerId = customerId;
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getDate() {
        return date;
    }
}

class VendorNameId {
    private String id;
    private String name;

    public VendorNameId(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class VendorItemsInfo {
    public int index;
    public String itemId;
    public String itemName;
    public Double price;

    public VendorItemsInfo(int index, String itemId, String itemName, double price) {
        this.index = index;
        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
    }

    public int getIndex() {
        return index;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }
}

class ReplyInfo {
    private int index;
    private String date;
    private String reply;
    private String managerId;

    public ReplyInfo(int index, String date, String reply, String managerId) {
        this.index = index;
        this.date = date;
        this.reply = reply;
        this.managerId = managerId;
    }

    public String getDate() {
        return date;
    }

    public String getReply() {
        return reply;
    }

    public String getManagerId() {
        return managerId;
    }

    public int getIndex() {
        return index;
    }
}

public class ManagerController {
    @FXML
    private HBox navbarContent;

    @FXML
    private VBox content;

    @FXML
    private Label nameLabel;

    @FXML
    private Button vendorPerformanceButton, runnerPerformanceButton, resolveComplaintsButton, discardVendorItemButton;

    Orders ordersDB = new Orders();

    String managerId = App.getData().getString("id");

    private void setActiveButton(Button selected) {
        Button[] navButtons = {
                vendorPerformanceButton,
                runnerPerformanceButton,
                resolveComplaintsButton,
                discardVendorItemButton
        };

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active-button");
        }

        if (!selected.getStyleClass().contains("active-button")) {
            selected.getStyleClass().add("active-button");
        }
    }

    @FXML
    private void initialize() {
        Navbar navbar = new Navbar("Manager", App.getData().getString("name"));
        HBox.setHgrow(navbar, Priority.ALWAYS);
        navbarContent.getChildren().add(navbar);

        vendorPerformanceButton.setOnAction(e -> {
            setActiveButton(vendorPerformanceButton);
            showVendorPerformance();
        });
        runnerPerformanceButton.setOnAction(e -> {
            setActiveButton(runnerPerformanceButton);
            showRunnerPerformance();
        });
        resolveComplaintsButton.setOnAction(e -> {
            setActiveButton(resolveComplaintsButton);
            showResolveComplaints();
        });
        discardVendorItemButton.setOnAction(e -> {
            setActiveButton(discardVendorItemButton);
            showDiscardVendorItems();
        });

        setActiveButton(vendorPerformanceButton);
        showVendorPerformance();
    }

    private void showVendorPerformance() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Date");
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                LocalDate date = Instant.ofEpochMilli(object.longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                return date.format(formatter);
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Revenue");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Vendor Performance");

        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;

        Map<String, XYChart.Series<Number, Number>> seriesMap = new HashMap<>();
        JSONArray allCompletedOrders = ordersDB.getAllCompletedOrders();
        for (Object element : allCompletedOrders) {
            JSONObject order = (JSONObject) element;

            String vendorId = order.getString("vendorId");
            seriesMap.computeIfAbsent(vendorId, k -> {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(Vendors.getVendorName(vendorId));

                return series;
            });

            String dateStr = order.getString("date");
            double total = order.getDouble("total");

            LocalDate date = LocalDate.parse(dateStr, formatter);
            long timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

            minTimestamp = Math.min(minTimestamp, timestamp);
            maxTimestamp = Math.max(maxTimestamp, timestamp);

            XYChart.Series<Number, Number> series = seriesMap.get(vendorId);
            series.getData().add(new XYChart.Data<>(timestamp, total));
        }

        LocalDate minDate = Instant.ofEpochMilli(minTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .minusMonths(1);
        LocalDate maxDate = Instant.ofEpochMilli(maxTimestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusMonths(1);

        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(minDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        xAxis.setUpperBound(maxDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());

        // Set tick unit to 7 days (in milliseconds)
        long weekInMillis = 7 * 24 * 61 * 60 * 1000;
        xAxis.setTickUnit(weekInMillis);
        xAxis.setMinorTickCount(0);

        for (Map.Entry<String, XYChart.Series<Number, Number>> entry : seriesMap.entrySet()) {
            lineChart.getData().add(entry.getValue());
        }

        content.getChildren().clear();
        content.getChildren().add(lineChart);
    }

    private void showRunnerPerformance() {
        Runners runnersDB = new Runners();

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Runner name");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Rating");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Runner Performance");

        Map<String, BarChart.Series<String, Number>> seriesMap = new HashMap<>();
        JSONArray runnersArray = runnersDB.getData();
        for (Object element : runnersArray) {
            JSONObject runner = (JSONObject) element;

            String runnerId = runner.getString("id");
            String runnerNmae = runner.getString("name");
            double avgRating = runner.getDouble("avgRating");

            seriesMap.computeIfAbsent(runnerId, k -> {
                BarChart.Series<String, Number> series = new BarChart.Series<>();
                series.setName(runnerNmae);

                return series;
            });

            XYChart.Series<String, Number> series = seriesMap.get(runnerId);
            series.getData().add(new XYChart.Data<>(runnerNmae, avgRating));
        }

        for (Map.Entry<String, XYChart.Series<String, Number>> entry : seriesMap.entrySet()) {
            barChart.getData().add(entry.getValue());
        }

        content.getChildren().clear();
        content.getChildren().add(barChart);
    }

    private void showResolveComplaints() {
        Customers customersDB = new Customers();
        Complaints complaintsDB = new Complaints();

        ObservableList<ComplaintInfoExtra> complaintList = FXCollections.observableArrayList();

        Runnable updateComplaintList = () -> {
            complaintList.clear();
            for (Object element : complaintsDB.getData()) {
                JSONObject complaint = (JSONObject) element;
                complaintList.add(
                        new ComplaintInfoExtra(complaint.getString("orderId"),
                                complaint.getString("complaintTitle"),
                                complaint.getString("description"), complaint.getJSONArray("replies"),
                                complaint.getString("status"), complaint.getString("id"),
                                complaint.getString("customerId"), complaint.getString("date")));
            }
        };

        updateComplaintList.run();

        ListView<ComplaintInfoExtra> complaintListView = new ListView<>(complaintList);

        complaintListView.setCellFactory(lv -> new ListCell<ComplaintInfoExtra>() {
            @Override
            protected void updateItem(ComplaintInfoExtra complaint, boolean empty) {
                super.updateItem(complaint, empty);
                if (empty || complaint == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);

                    Label orderIdLabel = new Label("Order ID: " + complaint.getOrderId());
                    Label titleLabel = new Label("Title: " + complaint.getTitle());
                    Label descriptionLabel = new Label("Description: " + complaint.getDescription());
                    Label statusLabel = new Label("Status: " + complaint.getStatus());
                    Label dateLabel = new Label(HumanDate.convert(complaint.getDate()));

                    Button markAsResolvedButton = new Button(
                            complaint.getStatus().equals("pending") ? "Mark as resolved" : "Mark as pending");
                    markAsResolvedButton.setOnAction(e -> {
                        JSONObject complaintObject = complaintsDB.getById(complaint.getId());
                        complaintObject.put("status", complaint.getStatus().equals("pending") ? "resolved" : "pending");
                        complaintsDB.updateFileContent();

                        updateComplaintList.run();
                    });

                    Button resolveByRefunding = new Button("Resolve by refunding");
                    resolveByRefunding.setDisable(complaint.getStatus().equals("resolved"));
                    resolveByRefunding.setOnAction(e -> {
                        JSONObject complaintObject = complaintsDB.getById(complaint.getId());
                        complaintObject.put("status", "resolved");
                        complaintsDB.updateFileContent();

                        JSONObject orderObject = ordersDB.getById(complaint.getOrderId());
                        double orderTotal = orderObject.getDouble("total") + orderObject.getDouble("deliveryFee");

                        JSONObject customerObject = customersDB.getById(complaint.getCustomerId());
                        double customerCredits = customerObject.getDouble("credits");

                        customerObject.put("credits", customerCredits + orderTotal);
                        customersDB.updateFileContent();

                        updateComplaintList.run();
                    });

                    HBox buttonsContainer = new HBox(10, markAsResolvedButton, resolveByRefunding);

                    Label repliesLabel = new Label("Replies");
                    ObservableList<ReplyInfo> replies = FXCollections.observableArrayList();
                    for (int i = 0; i < complaint.getReplies().length(); i++) {
                        JSONObject reply = (JSONObject) complaint.getReplies().get(i);
                        replies.add(new ReplyInfo(i, reply.getString("date"), reply.getString("reply"),
                                reply.getString("managerId")));
                    }
                    ListView<ReplyInfo> repliesListView = new ListView<>(replies);
                    repliesListView.setCellFactory(lv -> new ListCell<ReplyInfo>() {
                        @Override
                        protected void updateItem(ReplyInfo reply, boolean empty) {
                            super.updateItem(reply, empty);
                            if (empty || reply == null) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                HBox replyContainer = new HBox();
                                VBox replyVbox = new VBox(5);

                                Label replyDateLabel = new Label(HumanDate.convert(reply.getDate()));
                                Label managerNameLabel = new Label(
                                        "Manager: " + Managers.getManagerName(reply.getManagerId()));
                                Label replyLabel = new Label(reply.getReply());

                                replyVbox.getChildren().addAll(replyDateLabel, managerNameLabel, replyLabel);
                                HBox.setHgrow(replyVbox, Priority.ALWAYS);

                                Button trashButton = new Button();
                                trashButton.setGraphic(new TrashIcon().svg);
                                trashButton.getStyleClass().add("trash-button");
                                trashButton.setOnAction(e -> {
                                    JSONObject complaintObject = complaintsDB.getById(complaint.getId());
                                    complaintObject.getJSONArray("replies").remove(reply.getIndex());
                                    complaintsDB.updateFileContent();

                                    updateComplaintList.run();
                                });

                                replyContainer.getChildren().addAll(replyVbox, trashButton);
                                replyContainer.setAlignment(Pos.CENTER_LEFT);

                                CellContainer container = new CellContainer(replyContainer);
                                setGraphic(container);
                            }
                        }
                    });

                    // Allow cells to have variable heights
                    repliesListView.setFixedCellSize(-1);

                    // Once the skin is available, bind the ListView's prefHeight to the height of
                    // the virtual flow
                    repliesListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                        if (newSkin != null) {
                            // Look up the internal "virtual-flow" node responsible for layout
                            Node virtualFlow = repliesListView.lookup(".virtual-flow");
                            if (virtualFlow != null) {
                                repliesListView.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                                    // This calculates the preferred height dynamically based on the virtual flow's
                                    // bounds
                                    return virtualFlow.prefHeight(-1);
                                }, virtualFlow.layoutBoundsProperty()));
                            }
                        }
                    });

                    Button addReplyButton = new Button("Add reply");
                    addReplyButton.setOnAction(e -> {
                        Dialog<String> dialog = new Dialog<>();
                        dialog.setTitle("Add reply");
                        dialog.setHeaderText("What do you want to say?");

                        GridPane grid = new GridPane();
                        grid.setHgap(10);
                        grid.setVgap(10);
                        grid.setPadding(new Insets(20));

                        Label replyLabel = new Label("Reply:");

                        TextField replyField = new TextField();

                        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                        grid.add(replyLabel, 0, 0);
                        grid.add(replyField, 1, 0);

                        dialog.getDialogPane().setContent(grid);

                        dialog.setResultConverter(dialogButton -> {
                            if (dialogButton == ButtonType.OK) {
                                String reply = replyField.getText();
                                JSONObject repcomplaintObject = complaintsDB.getById(complaint.getId());
                                JSONArray repliesArray = repcomplaintObject.getJSONArray("replies");
                                String currentDate = LocalDate.now()
                                        .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

                                repliesArray
                                        .put(new JSONObject()
                                                .put("date", currentDate)
                                                .put("reply", reply).put("managerId", managerId));
                                complaintsDB.updateFileContent();

                                updateComplaintList.run();
                                return null;
                            }
                            return null;
                        });

                        dialog.showAndWait();
                    });

                    vbox.getChildren().addAll(orderIdLabel, dateLabel, titleLabel, descriptionLabel, statusLabel,
                            buttonsContainer, repliesLabel, repliesListView, addReplyButton);

                    CellContainer container = new CellContainer(vbox);
                    setGraphic(container);
                }
            }
        });

        VBox.setVgrow(complaintListView, Priority.ALWAYS);

        content.getChildren().clear();
        content.getChildren().add(complaintListView);
    }

    public void showDiscardVendorItems() {
        Vendors vendorsDB = new Vendors();

        VBox menuContainer = new VBox(10);

        ObservableList<VendorNameId> vendorNames = FXCollections.observableArrayList();

        JSONArray vendorsArray = vendorsDB.getData();
        for (Object element : vendorsArray) {
            JSONObject json = (JSONObject) element;
            vendorNames.add(new VendorNameId(json.getString("id"), json.getString("name")));
        }

        ComboBox<VendorNameId> vendorComboBox = new ComboBox<>();
        vendorComboBox.setItems(vendorNames);

        vendorComboBox.setCellFactory(lv -> new ListCell<VendorNameId>() {
            @Override
            protected void updateItem(VendorNameId item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        vendorComboBox.setButtonCell(new ListCell<VendorNameId>() {
            @Override
            protected void updateItem(VendorNameId item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        if (!vendorNames.isEmpty()) {
            vendorComboBox.getSelectionModel().selectFirst();
        }

        ObservableList<VendorItemsInfo> vendorItems = FXCollections.observableArrayList();
        Runnable updateVendorItems = () -> {
            Items itemsDB = new Items();
            VendorNameId selectedVendor = vendorComboBox.getSelectionModel().getSelectedItem();
            if (selectedVendor != null) {
                vendorItems.clear();
                JSONArray itemsArray = vendorsDB.getItems(selectedVendor.getId());
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject itemObject = itemsArray.getJSONObject(i);
                    JSONObject selectedItem = itemsDB.getById(itemObject.getString("id"));

                    vendorItems.add(new VendorItemsInfo(i, selectedItem.getString("id"),
                            selectedItem.getString("itemName"), selectedItem.getDouble("price")));
                }
            }
        };
        updateVendorItems.run();

        vendorComboBox.setOnAction(event -> {
            updateVendorItems.run();
        });

        TableView<VendorItemsInfo> itemsTable = new TableView<>();
        itemsTable.setItems(vendorItems);

        itemsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<VendorItemsInfo, String> itemIdCol = new TableColumn<>("itemId");
        itemIdCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getItemId()));

        TableColumn<VendorItemsInfo, String> itemNameCol = new TableColumn<>("itemName");
        itemNameCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getItemName()));

        TableColumn<VendorItemsInfo, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice()));

        itemsTable.getColumns().addAll(itemIdCol, itemNameCol, priceCol);

        itemsTable.setRowFactory(tv -> {
            TableRow<VendorItemsInfo> row = new TableRow<>();
            // Create the context menu with a "Delete Selected" item.
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteSelected = new MenuItem("Delete Selected");
            deleteSelected.setOnAction(event -> {
                Items itemsDB = new Items();
                VendorNameId selectedVendor = vendorComboBox.getSelectionModel().getSelectedItem();
                if (selectedVendor != null) {
                    // Gather all currently selected items
                    ObservableList<VendorItemsInfo> selectedItems = itemsTable.getSelectionModel().getSelectedItems();
                    if (!selectedItems.isEmpty()) {
                        List<VendorItemsInfo> itemsToDelete = new ArrayList<>(selectedItems);

                        for (VendorItemsInfo itemInfo : itemsToDelete) {
                            itemsDB.removeItem(itemInfo.getItemId());
                        }

                        vendorItems.removeAll(itemsToDelete);
                    }
                }
            });
            contextMenu.getItems().add(deleteSelected);

            // Show context menu only for non-empty rows
            row.contextMenuProperty().bind(
                    Bindings.when(Bindings.isNotNull(row.itemProperty())).then(contextMenu)
                            .otherwise((ContextMenu) null));

            return row;
        });

        menuContainer.getChildren().addAll(vendorComboBox, itemsTable);

        content.getChildren().clear();
        content.getChildren().add(menuContainer);
    }
}