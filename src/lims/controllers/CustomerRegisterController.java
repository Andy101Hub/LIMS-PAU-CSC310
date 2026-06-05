/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lims.db.DBConnection;
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

    @FXML
    private void registerCustomer() {

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

        if (!password.equals(confirmPassword)) {

            showAlert(Alert.AlertType.ERROR,
                    "Registration Error",
                    "Passwords do not match.");

            return;
        }

        try {

            Connection conn = DBConnection.getConnection();

            String checkSql =
                    "SELECT * FROM users WHERE email = ?";

            PreparedStatement checkStmt =
                    conn.prepareStatement(checkSql);

            checkStmt.setString(1, email);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {

                showAlert(Alert.AlertType.ERROR,
                        "Registration Error",
                        "Email already exists.");

                return;
            }

            String hashedPassword =
                    BCrypt.hashpw(password, BCrypt.gensalt());

            String insertSql =
                    "INSERT INTO users "
                    + "(full_name, email, password_hash, role, force_password_change, email_verified) "
                    + "VALUES (?, ?, ?, 'CUSTOMER', FALSE, FALSE)";

            PreparedStatement insertStmt =
                    conn.prepareStatement(insertSql);

            insertStmt.setString(1, fullName);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);

            insertStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION,
                    "Success",
                    "Customer account created successfully.");

            clearFields();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    e.getMessage());
        }
    }

    private void clearFields() {

        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
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
