package com.g25;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.g25.db.Vendors;
import com.g25.db.Customers;
import com.g25.db.Requests;
import com.g25.db.Runners;
import com.g25.db.Users;
import com.g25.admin.CustomerRequestInfo;
import com.g25.admin.Person;
import com.g25.customcomponent.CellContainer;
import com.g25.customcomponent.Navbar;

public class AdminController {

    @FXML
    private HBox navbarContent;

    @FXML
    private VBox content, topupRequestsContent;

    @FXML
    private Label roleLabel;

    @FXML
    private TableView<Person> dataTable;

    @FXML
    private Button createButton, vendorButton, customerButton, runnerButton, topupRequestsButton;

    // Create separate DB instances.
    private Vendors vendorsDB;
    private Customers customersDB;
    private Runners runnersDB;

    private Requests requestsDB = new Requests();

    // Use currentDB to track active database.
    private com.g25.db.DBCRUD currentDB;

    private void setActiveButton(Button selected) {
        Button[] navButtons = {
                vendorButton,
                customerButton,
                runnerButton,
                topupRequestsButton
        };

        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active-button");
        }

        if (!selected.getStyleClass().contains("active-button")) {
            selected.getStyleClass().add("active-button");
        }
    }

    private void showTopupRequests() {
        content.setVisible(false);
        content.setManaged(false);

        topupRequestsContent.setVisible(true);
        topupRequestsContent.setManaged(true);

        ObservableList<CustomerRequestInfo> requests = FXCollections.observableArrayList();

        for (Object element : requestsDB.getDetachedData()) {
            JSONObject request = (JSONObject) element;

            if (request.getString("status").equals("pending")) {
                requests.add(new CustomerRequestInfo(request.getString("id"), request.getString("customerId"),
                        request.getDouble("topupValue")));
            }
        }

        ListView<CustomerRequestInfo> requestList = new ListView<>(requests);
        requestList.setCellFactory(lv -> new ListCell<CustomerRequestInfo>() {
            @Override
            protected void updateItem(CustomerRequestInfo request, boolean empty) {
                super.updateItem(request, empty);
                if (empty || request == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox requestContainer = new VBox();

                    Label customerNameLabel = new Label(Customers.getCustomerName(request.getCustomerId()));
                    Label topupValueLabel = new Label(String.valueOf(request.getTopupValue()));

                    Button processButton = new Button("Process");
                    processButton.setOnAction(e -> {
                        requestsDB.acceptRequest(request.getId());

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Topup successfully processed");
                        alert.setHeaderText("The customer's credit has been updated.");
                        alert.showAndWait();

                        requestList.getItems().remove(request);
                        getListView().refresh();
                    });

                    requestContainer.getChildren().addAll(customerNameLabel, topupValueLabel, processButton);

                    CellContainer container = new CellContainer(requestContainer);

                    setGraphic(container);
                }
            }
        });

        topupRequestsContent.getChildren().clear();
        topupRequestsContent.getChildren().add(requestList);
    }

    @FXML
    private void initialize() {
        Navbar navbar = new Navbar("Admin", App.getData().getString("name"));
        HBox.setHgrow(navbar, Priority.ALWAYS);
        navbarContent.getChildren().add(navbar);

        // Instantiate DB objects and their corresponding wrappers.
        vendorsDB = new Vendors();
        customersDB = new Customers();
        runnersDB = new Runners();

        // Set the default DB to vendors.
        currentDB = vendorsDB;
        topupRequestsContent.setManaged(false);
        loadData();
        roleLabel.setText("Showing Vendors");
        setActiveButton(vendorButton);

        // Set up button actions.
        vendorButton.setOnAction(e -> {
            setActiveButton(vendorButton);
            content.setVisible(true);
            content.setManaged(true);

            topupRequestsContent.setVisible(false);
            topupRequestsContent.setManaged(false);

            currentDB = vendorsDB;
            roleLabel.setText("Showing Vendors");
            loadData();
        });

        customerButton.setOnAction(e -> {
            setActiveButton(customerButton);
            content.setVisible(true);
            content.setManaged(true);

            topupRequestsContent.setVisible(false);
            topupRequestsContent.setManaged(false);

            currentDB = customersDB;
            roleLabel.setText("Showing Customers");
            loadData();
        });

        runnerButton.setOnAction(e -> {
            setActiveButton(runnerButton);
            content.setVisible(true);
            content.setManaged(true);

            topupRequestsContent.setVisible(false);
            topupRequestsContent.setManaged(false);

            currentDB = runnersDB;
            roleLabel.setText("Showing Runners");
            loadData();
        });

        topupRequestsButton.setOnAction(e -> {
            setActiveButton(topupRequestsButton);
            showTopupRequests();
        });

        // Set table to be editable with multiple row-selection.
        dataTable.setEditable(true);
        dataTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setupEditableColumns();
        setupRowContextMenu();
    }

    /**
     * Loads data from the currentDB and updates the table items.
     */
    private void loadData() {
        JSONArray dataArray = currentDB.getDetachedData();
        ObservableList<Person> people = FXCollections.observableArrayList();

        for (Object element : dataArray) {
            JSONObject json = (JSONObject) element;
            people.add(new Person(
                    json.getString("name"),
                    json.getString("id"),
                    json.getString("email"),
                    json.getString("address"),
                    json.getString("phone")));
        }
        dataTable.setItems(people);
    }

    /**
     * Set up inline editing for all columns except the "ID" column.
     */
    private void setupEditableColumns() {
        for (TableColumn<Person, ?> column : dataTable.getColumns()) {
            String colName = column.getText();
            if ("ID".equalsIgnoreCase(colName)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            TableColumn<Person, String> stringColumn = (TableColumn<Person, String>) column;
            stringColumn.setCellFactory(TextFieldTableCell.forTableColumn());

            stringColumn.setOnEditCommit(event -> {
                TablePosition<Person, String> pos = event.getTablePosition();
                String newValue = event.getNewValue();
                String columnName = event.getTableColumn().getText();
                int rowIndex = pos.getRow();
                Person person = event.getRowValue();

                System.out.printf("Edited row: %d | Column: %s | New value: %s%n",
                        rowIndex, columnName, newValue);

                updatePersonField(person, columnName, newValue);
                updateJSONForPerson(person);
            });
        }
    }

    /**
     * Update a Person's field based on the column name.
     */
    private void updatePersonField(Person person, String columnName, String newValue) {
        switch (columnName) {
            case "Name":
                person.setName(newValue);
                break;
            case "Email":
                person.setEmail(newValue);
                break;
            case "Address":
                person.setAddress(newValue);
                break;
            case "Phone":
                person.setPhone(newValue);
                break;
        }
    }

    /**
     * Updates the JSON representation for the given person in the current DB.
     */
    private void updateJSONForPerson(Person person) {
        ObservableList<Person> people = dataTable.getItems();
        for (int i = 0; i < people.size(); i++) {
            Person p = people.get(i);
            if (p.getId().equals(person.getId())) {
                JSONObject updatedJSON = new JSONObject();
                updatedJSON.put("id", p.getId());
                updatedJSON.put("name", p.getName());
                updatedJSON.put("email", p.getEmail());
                updatedJSON.put("address", p.getAddress());
                updatedJSON.put("phone", p.getPhone());

                currentDB.update(i, updatedJSON);
                break;
            }
        }
    }

    /**
     * Creates a context menu with deletion options (batch deletion) and
     * attaches it to each non-empty table row.
     */
    private void setupRowContextMenu() {
        dataTable.setRowFactory(tv -> {
            TableRow<Person> row = new TableRow<>();
            ContextMenu contextMenu = createDeleteContextMenu();
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
            return row;
        });
    }

    /**
     * Returns a context menu with an option to delete the currently selected rows.
     */
    private ContextMenu createDeleteContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Delete Selected");
        deleteItem.setOnAction(e -> {
            ObservableList<Person> selectedPeople = dataTable.getSelectionModel().getSelectedItems();
            if (!selectedPeople.isEmpty()) {
                List<Person> toRemove = new ArrayList<>(selectedPeople);
                for (Person person : toRemove) {
                    dataTable.getItems().remove(person);
                    currentDB.delete(person.getId());
                    Users usersDB = new Users();
                    usersDB.delete(person.getId());
                    usersDB.updateFileContent();
                }
                System.out.printf("Deleted %d entries%n", toRemove.size());
            }
        });
        contextMenu.getItems().add(deleteItem);
        return contextMenu;
    }

    /**
     * Handler for the Create button that loads a pop-up dialog.
     * Prevents creation if any required field is empty.
     */
    @FXML
    private void handleCreate() {
        Dialog<Pair<String, String[]>> dialog = new Dialog<>();
        dialog.setTitle("Create New Entry");
        dialog.setHeaderText("Fill in details:");

        GridPane grid = createFormGrid();
        dialog.getDialogPane().setContent(grid);

        // Add OK and Cancel buttons.
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);

        // Bind the disable property of the OK button to whether any required field is
        // empty.
        TextField[] fields = getFormFields(grid);
        okButton.disableProperty().bind(requiredFieldsEmptyBinding(fields));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String newId = UUID.randomUUID().toString();
                // Extract the values in order: name, email, address, phone
                String[] fieldData = new String[] {
                        fields[0].getText().trim(),
                        fields[1].getText().trim(),
                        fields[2].getText().trim(),
                        fields[3].getText().trim()
                };
                return new Pair<>(newId, fieldData);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String newId = result.getKey();
            String[] fieldsResult = result.getValue();
            String newName = fieldsResult[0];
            String newEmail = fieldsResult[1];
            String newAddress = fieldsResult[2];
            String newPhone = fieldsResult[3];

            Person newPerson = new Person(newName, newId, newEmail, newAddress, newPhone);
            dataTable.getItems().add(newPerson);
            currentDB.add(newPerson);
            System.out.printf("Created entry: %s%n", newName);
        });
    }

    /**
     * Creates and returns a GridPane containing labels (with required field
     * indicators) and TextFields for entry details.
     */
    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Labels with required field indicators.
        Label lblName = new Label("Name *");
        Label lblEmail = new Label("Email *");
        Label lblAddress = new Label("Address *");
        Label lblPhone = new Label("Phone *");

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        // Place controls into the grid.
        grid.add(lblName, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(lblEmail, 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(lblAddress, 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(lblPhone, 0, 3);
        grid.add(phoneField, 1, 3);

        return grid;
    }

    /**
     * Returns an array of the TextField controls from the vendor form grid.
     */
    private TextField[] getFormFields(GridPane grid) {
        TextField[] fields = new TextField[4];
        for (Node node : grid.getChildren()) {
            if (node instanceof TextField) {
                Integer rowIndex = GridPane.getRowIndex(node);
                if (rowIndex != null && rowIndex >= 0 && rowIndex < fields.length) {
                    fields[rowIndex] = (TextField) node;
                }
            }
        }
        return fields;
    }

    /**
     * Returns a BooleanBinding which is true when any of the provided TextFields is
     * empty.
     */
    private BooleanBinding requiredFieldsEmptyBinding(TextField... fields) {
        return Bindings.createBooleanBinding(() -> {
            for (TextField field : fields) {
                if (field.getText().trim().isEmpty()) {
                    return true;
                }
            }
            return false;
        }, fields[0].textProperty(), fields[1].textProperty(), fields[2].textProperty(), fields[3].textProperty());
    }
}
