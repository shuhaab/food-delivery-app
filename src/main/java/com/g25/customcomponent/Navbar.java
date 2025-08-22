package com.g25.customcomponent;

import java.io.IOException;
import java.util.Optional;

import com.g25.App;
import com.g25.db.Users;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class Navbar extends HBox {
  Users usersDB = new Users();

  public Navbar(String role, String name) {
    super();
    getStyleClass().add("navbar");

    setMaxWidth(Region.USE_PREF_SIZE);
    setPrefWidth(Region.USE_COMPUTED_SIZE);
    setMaxWidth(Double.MAX_VALUE);

    setAlignment(Pos.CENTER_LEFT);
    setSpacing(10);
    setPadding(new Insets(10));

    Button logoutButton = new Button("Logout");
    logoutButton.setOnAction(e -> {
      Alert alert = new Alert(AlertType.CONFIRMATION);
      alert.setHeaderText("Logout?");
      alert.setContentText(
          "Are you sure you want to log out?");

      alert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
          try {
            App.setRoot("login");
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        }
      });
    });

    Button editInfoButton = new Button("Change password");
    editInfoButton.setOnAction(e -> {
      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Edit Info");
      dialog.setHeaderText("Fill in the information");

      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField passwordField = new TextField();
      passwordField.setPromptText("Password");
      passwordField.setText(usersDB.getById(App.getData().getString("id")).getString("password"));

      grid.add(new Label("Password:"), 0, 1);
      grid.add(passwordField, 1, 1);

      dialog.getDialogPane().setContent(grid);

      ButtonType updateButtonType = new ButtonType("Update", ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

      Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == updateButtonType) {
        usersDB.updatePassword(App.getData().getString("id"), passwordField.getText());
        usersDB.refreshData();
      }
    });

    HBox rightContainer = new HBox();
    Label roleLabel = new Label(role + " menu");
    Label nameLabel = new Label("Logged in as " + name);

    rightContainer.getChildren().addAll(roleLabel, nameLabel);
    rightContainer.setSpacing(10);
    rightContainer.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    getChildren().addAll(logoutButton, editInfoButton, rightContainer);
  }
}
