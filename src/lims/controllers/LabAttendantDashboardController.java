package lims.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import lims.models.User;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class LabAttendantDashboardController {

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
    private void openLabRequestQueue(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/lab_test_request_queue.fxml",
                "Lab Test Request Queue"
        );
    }

    @FXML
    private void openSampleTracking(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/sample_tracking.fxml",
                "Sample Lifecycle Tracking"
        );
    }

    @FXML
    private void openResultUploadValidation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/result_upload_validation.fxml",
                "Result Upload & Validation"
        );
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
    private void openCustomerProfiles(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/lab_customer_profiles.fxml",
            "Customer Profiles"
        );
    }
}