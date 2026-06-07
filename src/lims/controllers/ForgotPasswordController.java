package lims.controllers;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.utils.BCryptUtil;
import lims.utils.EmailService;
import lims.utils.SceneNavigator;

public class ForgotPasswordController {

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private final SecureRandom random = new SecureRandom();

    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "CUSTOMER",
                "LAB_ATTENDANT"
        ));
    }

    @FXML
    private void handleForgotPassword() {
        String role = roleComboBox.getValue();
        String email = emailField.getText().trim();

        if (role == null || email.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select account type and enter your email.");
            return;
        }

        if (!email.contains("@")) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        if (role.equals("CUSTOMER")) {
            handleCustomerReset(email);
        } else if (role.equals("LAB_ATTENDANT")) {
            handleLabAttendantResetRequest(email);
        }
    }

    private void handleCustomerReset(String email) {
        try {
            UserRecord user = findUserByEmailAndRole(email, "CUSTOMER");

            if (user == null) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("No customer account was found with this email.");
                return;
            }

            String temporaryPassword = generateTemporaryPassword();
            String hashedPassword = BCryptUtil.hashPassword(temporaryPassword);

            updateUserTemporaryPassword(user.userId, hashedPassword);

            EmailService.sendTemporaryPassword(
                    user.email,
                    user.fullName,
                    temporaryPassword
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("A temporary password has been sent to your email. Please log in and change it immediately.");

            emailField.clear();
            roleComboBox.setValue(null);

        } catch (Exception e) {
            e.printStackTrace();

            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not send temporary password. Please check email settings or internet connection.");
        }
    }

    private void handleLabAttendantResetRequest(String email) {
        try {
            UserRecord user = findUserByEmailAndRole(email, "LAB_ATTENDANT");

            if (user == null) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("No Lab Attendant account was found with this email.");
                return;
            }

            if (hasPendingResetRequest(user.userId)) {
                messageLabel.setStyle("-fx-text-fill: #6b7280;");
                messageLabel.setText("A reset request is already pending. Please wait for the Super Admin.");
                return;
            }

            createResetRequest(user.userId, user.email, "LAB_ATTENDANT");

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Super Admin has been notified. You will receive a temporary password from the Super Admin.");

            emailField.clear();
            roleComboBox.setValue(null);

        } catch (SQLException e) {
            e.printStackTrace();

            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not create password reset request.");
        }
    }

    private UserRecord findUserByEmailAndRole(String email, String role) throws SQLException {
        String sql = "SELECT user_id, full_name, email "
                   + "FROM users "
                   + "WHERE email = ? AND role = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, email);
        stmt.setString(2, role);

        ResultSet rs = stmt.executeQuery();

        UserRecord user = null;

        if (rs.next()) {
            user = new UserRecord(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email")
            );
        }

        rs.close();
        stmt.close();
        conn.close();

        return user;
    }

    private void updateUserTemporaryPassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users "
                   + "SET password_hash = ?, force_password_change = TRUE "
                   + "WHERE user_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, hashedPassword);
        stmt.setInt(2, userId);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private boolean hasPendingResetRequest(int userId) throws SQLException {
        String sql = "SELECT reset_request_id "
                   + "FROM password_reset_requests "
                   + "WHERE user_id = ? AND status = 'PENDING'";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();

        boolean exists = rs.next();

        rs.close();
        stmt.close();
        conn.close();

        return exists;
    }

    private void createResetRequest(int userId, String email, String role) throws SQLException {
        String sql = "INSERT INTO password_reset_requests "
                   + "(user_id, email, role, status) "
                   + "VALUES (?, ?, ?, 'PENDING')";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, userId);
        stmt.setString(2, email);
        stmt.setString(3, role);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private String generateTemporaryPassword() {
        int number = 100000 + random.nextInt(900000);
        return "Temp@" + number;
    }

    @FXML
    private void backToLogin(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/login.fxml",
                "LIMS Login"
        );
    }

    private static class UserRecord {
        int userId;
        String fullName;
        String email;

        UserRecord(int userId, String fullName, String email) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
        }
    }
}