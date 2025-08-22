package com.g25;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.g25.db.Users;

import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private Label errorLabel;

    @FXML
    private TextField nameInput, passwordInput;

    @FXML
    private void initialize() {
        nameInput.setOnAction(e -> {
            Login();
        });

        passwordInput.setOnAction(e -> {
            Login();
        });
    }

    @FXML
    private void Login() {
        try {
            Users users = new Users();
            JSONArray userArray = users.getDetachedData();

            boolean found = false;
            String role = "";

            JSONObject userData = new JSONObject();
            for (Object element : userArray) {
                JSONObject user = (JSONObject) element;

                if (user.getString("name").equals(nameInput.getText())
                        && user.getString("password").equals(passwordInput.getText())) {
                    found = true;
                    role = user.getString("role");

                    userData = user;
                    break;
                }
            }

            if (found) {
                System.out.println("logged in" + " as " + role);
                App.setData(userData);
                App.setRoot(role);
            } else {
                errorLabel.setVisible(true);

                PauseTransition visiblePause = new PauseTransition(Duration.seconds(2));

                visiblePause.setOnFinished(event -> {
                    errorLabel.setVisible(false);
                });
                visiblePause.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
