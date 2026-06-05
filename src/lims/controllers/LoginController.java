package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import lims.db.DBConnection;
import lims.models.User;
import lims.utils.BCryptUtil;
import lims.utils.SessionManager;

import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import lims.utils.SceneNavigator;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter email and password.");
            return;
        }

        try {
            User user = findUserByEmail(email);

            if (user == null) {
                messageLabel.setText("Invalid email or password.");
                return;
            }

            boolean passwordMatches = BCryptUtil.checkPassword(password, user.getPasswordHash());

            if (!passwordMatches) {
                messageLabel.setText("Invalid email or password.");
                return;
            }

            SessionManager.setCurrentUser(user);

            if (user.isForcePasswordChange()) {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                SceneNavigator.switchScene(
                        stage,
                        "/lims/views/force_password_change.fxml",
                        "Change Password"
                );

                return;
            }

            openDashboardByRole(user, event);

        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        } catch (Exception e) {
            messageLabel.setText("Something went wrong.");
            e.printStackTrace();
        }
    }

    @FXML
    private void openCustomerRegister(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/customer_register.fxml",
                "Customer Registration"
        );
    }

    private User findUserByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, full_name, email, password_hash, role, force_password_change, email_verified "
                   + "FROM users WHERE email = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, email);

        ResultSet rs = stmt.executeQuery();

        User user = null;

        if (rs.next()) {
            user = new User(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getBoolean("force_password_change"),
                    rs.getBoolean("email_verified")
            );
        }

        rs.close();
        stmt.close();
        conn.close();

        return user;
    }

    private void openDashboardByRole(User user, ActionEvent event) {
        String role = user.getRole();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (role.equals("SUPER_ADMIN")) {
            SceneNavigator.switchScene(
                    stage,
                    "/lims/views/superadmin_dashboard.fxml",
                    "Super Admin Dashboard"
            );

        } else if (role.equals("LAB_ATTENDANT")) {
            showAlert("Login Successful", "Lab Attendant dashboard will be connected by Person 2.");

        } else if (role.equals("CUSTOMER")) {
            SceneNavigator.switchScene(
                    stage,
                    "/lims/views/customer_dashboard.fxml",
                    "Customer Dashboard"
            );

        } else {
            showAlert("Login Error", "Unknown user role: " + role);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}