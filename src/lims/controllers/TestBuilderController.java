package lims.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class TestBuilderController {

    @FXML
    private TextField testNameField;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField priceField;

    @FXML
    private TextField turnaroundTimeField;

    @FXML
    private ComboBox<String> resultFormatComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Blood",
                "Imaging",
                "Biopsy",
                "Urine",
                "Microbiology",
                "Other"
        ));

        resultFormatComboBox.setItems(FXCollections.observableArrayList(
                "NUMERIC",
                "TEXT",
                "PDF",
                "IMAGE"
        ));
    }

    @FXML
    private void handleCreateTestType() {
        String testName = testNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String priceText = priceField.getText().trim();
        String turnaroundText = turnaroundTimeField.getText().trim();
        String resultFormat = resultFormatComboBox.getValue();

        if (testName.isEmpty() || category == null || priceText.isEmpty()
                || turnaroundText.isEmpty() || resultFormat == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        BigDecimal price;
        int turnaroundHours;

        try {
            price = new BigDecimal(priceText);

            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Price must be greater than zero.");
                return;
            }

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Price must be a valid number.");
            return;
        }

        try {
            turnaroundHours = Integer.parseInt(turnaroundText);

            if (turnaroundHours <= 0) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Turnaround time must be greater than zero.");
                return;
            }

        } catch (NumberFormatException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Turnaround time must be a whole number.");
            return;
        }

        try {
            createTestType(testName, category, price, turnaroundHours, resultFormat);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Test type created successfully.");

            clearFields();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    private void createTestType(String testName, String category, BigDecimal price,
                                int turnaroundHours, String resultFormat) throws SQLException {

        String sql = "INSERT INTO test_types "
                   + "(test_name, category, price, turnaround_time_hours, result_format, created_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, testName);
        stmt.setString(2, category);
        stmt.setBigDecimal(3, price);
        stmt.setInt(4, turnaroundHours);
        stmt.setString(5, resultFormat);
        stmt.setInt(6, SessionManager.getCurrentUserId());

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    private void clearFields() {
        testNameField.clear();
        categoryComboBox.setValue(null);
        priceField.clear();
        turnaroundTimeField.clear();
        resultFormatComboBox.setValue(null);
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