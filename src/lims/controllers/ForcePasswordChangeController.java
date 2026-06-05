package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.User;
import lims.utils.BCryptUtil;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class ForcePasswordChangeController {

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handlePasswordChange(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            messageLabel.setText("Please fill in both password fields.");
            return;
        }

        if (newPassword.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageLabel.setText("No logged-in user found.");
            return;
        }

        String hashedPassword = BCryptUtil.hashPassword(newPassword);

        try {
            updatePasswordInDatabase(currentUser.getUserId(), hashedPassword);
            
            insertAuditLog(
                    "FORCE_PASSWORD_CHANGE",
                    "User changed temporary password after first login."
            );

            currentUser.setPasswordHash(hashedPassword);
            currentUser.setForcePasswordChange(false);
            SessionManager.setCurrentUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            openDashboardByRole(currentUser, stage);

        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    private void updatePasswordInDatabase(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users "
                   + "SET password_hash = ?, force_password_change = FALSE "
                   + "WHERE user_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, hashedPassword);
        stmt.setInt(2, userId);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void openDashboardByRole(User user, Stage stage) {
        String role = user.getRole();

        if (role.equals("SUPER_ADMIN")) {
            SceneNavigator.switchScene(
                    stage,
                    "/lims/views/superadmin_dashboard.fxml",
                    "Super Admin Dashboard"
            );

        } else if (role.equals("LAB_ATTENDANT")) {
            messageLabel.setText("Lab Attendant dashboard will be connected by Person 2.");

        } else if (role.equals("CUSTOMER")) {
            messageLabel.setText("Customer dashboard will be connected by Person 3.");

        } else {
            messageLabel.setText("Unknown user role: " + role);
        }
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
}