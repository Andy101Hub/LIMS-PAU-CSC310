package lims.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lims.models.User;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class CustomerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }
    }

    @FXML
    private void openTestCatalogue(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/test_catalogue.fxml",
                "Available Tests"
        );
    }

    @FXML
    private void openMyRequests(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/my_requests.fxml",
                "My Test Requests"
        );
    }
}