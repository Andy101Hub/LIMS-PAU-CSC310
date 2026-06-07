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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.User;
import lims.utils.EmailService;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class EmailVerificationController {

    @FXML
    private Label instructionLabel;

    @FXML
    private TextField codeField;

    @FXML
    private Label messageLabel;

    private final SecureRandom random = new SecureRandom();

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            instructionLabel.setText(
                    "We sent a verification code to " + currentUser.getEmail()
            );
        }
    }

    @FXML
    private void handleVerifyCode(ActionEvent event) {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageLabel.setText("No customer session found. Please register again.");
            return;
        }

        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            messageLabel.setText("Please enter the verification code.");
            return;
        }

        try {
            Connection conn = DBConnection.getConnection();

            String sql =
                    "SELECT email_verification_code, email_verification_expires_at "
                    + "FROM users WHERE user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUser.getUserId());

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                messageLabel.setText("User account not found.");
                rs.close();
                stmt.close();
                conn.close();
                return;
            }

            String storedCode = rs.getString("email_verification_code");
            Timestamp expiryTimestamp = rs.getTimestamp("email_verification_expires_at");

            rs.close();
            stmt.close();

            if (storedCode == null || expiryTimestamp == null) {
                messageLabel.setText("No verification code found. Please resend the code.");
                conn.close();
                return;
            }

            LocalDateTime expiryTime = expiryTimestamp.toLocalDateTime();

            if (LocalDateTime.now().isAfter(expiryTime)) {
                messageLabel.setText("Verification code has expired. Please resend the code.");
                conn.close();
                return;
            }

            if (!enteredCode.equals(storedCode)) {
                messageLabel.setText("Incorrect verification code.");
                conn.close();
                return;
            }

            String updateSql =
                    "UPDATE users "
                    + "SET email_verified = TRUE, "
                    + "email_verification_code = NULL, "
                    + "email_verification_expires_at = NULL "
                    + "WHERE user_id = ?";

            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, currentUser.getUserId());
            updateStmt.executeUpdate();

            updateStmt.close();
            conn.close();

            currentUser.setEmailVerified(true);
            SessionManager.setCurrentUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            SceneNavigator.switchScene(
                    stage,
                    "/lims/views/customer_dashboard.fxml",
                    "Customer Dashboard"
            );

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Verification failed. Please try again.");
        }
    }

    @FXML
    private void handleResendCode() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageLabel.setText("No customer session found. Please register again.");
            return;
        }

        try {
            String newCode = generateCode();
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);

            Connection conn = DBConnection.getConnection();

            String sql =
                    "UPDATE users "
                    + "SET email_verification_code = ?, "
                    + "email_verification_expires_at = ? "
                    + "WHERE user_id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, newCode);
            stmt.setTimestamp(2, Timestamp.valueOf(expiryTime));
            stmt.setInt(3, currentUser.getUserId());

            stmt.executeUpdate();

            stmt.close();
            conn.close();

            EmailService.sendVerificationCode(
                    currentUser.getEmail(),
                    currentUser.getFullName(),
                    newCode
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("A new verification code has been sent.");

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not resend verification code.");
        }
    }

    private String generateCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}