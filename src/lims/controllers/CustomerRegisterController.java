package lims.controllers;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.User;
import lims.utils.EmailService;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

import org.mindrot.jbcrypt.BCrypt;

public class CustomerRegisterController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final SecureRandom random = new SecureRandom();

    @FXML
    private void registerCustomer(ActionEvent event) {

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (fullName.isEmpty()
                || email.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "All fields are required.");

            return;
        }

        if (!email.contains("@")) {
            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "Please enter a valid email address.");

            return;
        }

        if (password.length() < 6) {
            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "Password must be at least 6 characters.");

            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "Passwords do not match.");

            return;
        }

        try {
            Connection conn = DBConnection.getConnection();

            String checkSql = "SELECT * FROM users WHERE email = ?";

            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR,
                        "Registration Error",
                        "Email already exists.");

                rs.close();
                checkStmt.close();
                conn.close();

                return;
            }

            rs.close();
            checkStmt.close();

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            String verificationCode = generateCode();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

            String insertSql =
                    "INSERT INTO users "
                    + "(full_name, email, password_hash, role, force_password_change, email_verified, "
                    + "email_verification_code, email_verification_expires_at) "
                    + "VALUES (?, ?, ?, 'CUSTOMER', FALSE, FALSE, ?, ?) "
                    + "RETURNING user_id";

            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            insertStmt.setString(1, fullName);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, verificationCode);
            insertStmt.setTimestamp(5, Timestamp.valueOf(expiryTime));

            ResultSet insertedUserResult = insertStmt.executeQuery();

            if (insertedUserResult.next()) {
                int newUserId = insertedUserResult.getInt("user_id");

                User newCustomer = new User(
                        newUserId,
                        fullName,
                        email,
                        hashedPassword,
                        "CUSTOMER",
                        false,
                        false
                );

                SessionManager.setCurrentUser(newCustomer);

                insertedUserResult.close();
                insertStmt.close();
                conn.close();

                EmailService.sendVerificationCode(
                        email,
                        fullName,
                        verificationCode
                );

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                SceneNavigator.switchScene(
                        stage,
                        "/lims/views/email_verification.fxml",
                        "Email Verification"
                );
            }

        } catch (Exception e) {
            e.printStackTrace();

            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "Account was not completed. Please check your email settings or database connection.");
        }
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

    private String generateCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String message) {

        Alert alert = new Alert(type);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}