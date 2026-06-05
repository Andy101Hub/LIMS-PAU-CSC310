package lims.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import lims.utils.SessionManager;
import lims.utils.SceneNavigator;

public class SuperAdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUserFullName());
        } else {
            welcomeLabel.setText("Welcome");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SessionManager.logout();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/login.fxml",
                "LIMS Login"
        );
    }
    
    @FXML
    private void openUserManagement(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/user_management.fxml",
            "User Management"
        );
    }
    
    @FXML
    private void openTestBuilder(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/test_builder.fxml",
            "Custom Test Builder"
        );
    }
    
    @FXML
    private void openTestRequestQueue(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/test_request_queue.fxml",
            "Test Request Queue"
        );
    }
    
    @FXML
    private void openAuditTrail(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/audit_trail.fxml",
            "Audit Trail"
        );
    }
}