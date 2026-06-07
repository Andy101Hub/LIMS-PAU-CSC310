package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.utils.BCryptUtil;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class LabCustomerProfilesController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleCreateCustomer() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String temporaryPassword = temporaryPasswordField.getText();

        if (fullName.isEmpty() || email.isEmpty() || temporaryPassword.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        if (!email.contains("@")) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        if (temporaryPassword.length() < 6) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Temporary password must be at least 6 characters.");
            return;
        }

        String hashedPassword = BCryptUtil.hashPassword(temporaryPassword);

        try {
            createCustomer(fullName, email, hashedPassword);

            insertAuditLog(
                    "LAB_CREATE_CUSTOMER",
                    "Lab Attendant created CUSTOMER account for "
                    + fullName
                    + " ("
                    + email
                    + ")."
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Customer account created successfully. Customer must change password on first login.");

            clearFields();

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("An account with this email already exists.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Database error. Please try again.");
                e.printStackTrace();
            }
        }
    }

    private void createCustomer(String fullName,
                                String email,
                                String hashedPassword) throws SQLException {

        String sql = "INSERT INTO users "
                   + "(full_name, email, password_hash, role, force_password_change, email_verified, created_by) "
                   + "VALUES (?, ?, ?, 'CUSTOMER', TRUE, TRUE, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, fullName);
        stmt.setString(2, email);
        stmt.setString(3, hashedPassword);
        stmt.setInt(4, SessionManager.getCurrentUserId());

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void insertAuditLog(String action, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, details) "
                   + "VALUES (?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, SessionManager.getCurrentUserId());
        stmt.setString(2, action);
        stmt.setString(3, details);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void clearFields() {
        fullNameField.clear();
        emailField.clear();
        temporaryPasswordField.clear();
    }

    @FXML
    private void backToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/lab_attendant_dashboard.fxml",
                "Lab Attendant Dashboard"
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
}