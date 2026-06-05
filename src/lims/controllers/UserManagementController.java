package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.utils.BCryptUtil;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class UserManagementController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "LAB_ATTENDANT",
                "CUSTOMER"
        ));
    }

    @FXML
    private void handleCreateAccount() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String temporaryPassword = temporaryPasswordField.getText();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || temporaryPassword.isEmpty() || role == null) {
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
            createUser(fullName, email, hashedPassword, role);

            insertAuditLog(
                    "CREATE_USER",
                    "Created " + role + " account for " + fullName + " (" + email + ")"
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully. User must change password on first login.");

            clearFields();

        } catch (SQLException e) {
            if (e.getMessage().contains("duplicate key")) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("An account with this email already exists.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Database error. Please try again.");
                e.printStackTrace();
            }
        }
    }

    private void createUser(String fullName, String email, String hashedPassword, String role) throws SQLException {
        String sql = "INSERT INTO users "
                   + "(full_name, email, password_hash, role, force_password_change, email_verified, created_by) "
                   + "VALUES (?, ?, ?, ?, TRUE, TRUE, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, fullName);
        stmt.setString(2, email);
        stmt.setString(3, hashedPassword);
        stmt.setString(4, role);
        stmt.setInt(5, SessionManager.getCurrentUserId());

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
        roleComboBox.setValue(null);
    }

    @FXML
    private void goBackToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/superadmin_dashboard.fxml",
                "Super Admin Dashboard"
        );
    }
}