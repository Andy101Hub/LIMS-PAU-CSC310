package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.TestType;
import lims.models.User;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class TestCatalogueController {

    @FXML
    private TableView<TestType> testsTable;

    @FXML
    private TableColumn<TestType, String> nameColumn;

    @FXML
    private TableColumn<TestType, String> categoryColumn;

    @FXML
    private TableColumn<TestType, Double> priceColumn;

    @FXML
    private TableColumn<TestType, Integer> turnaroundColumn;

    @FXML
    private TableColumn<TestType, String> formatColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        turnaroundColumn.setCellValueFactory(new PropertyValueFactory<>("turnaroundTimeHours"));
        formatColumn.setCellValueFactory(new PropertyValueFactory<>("resultFormat"));

        loadTests();
    }

    private void loadTests() {
        ObservableList<TestType> tests = FXCollections.observableArrayList();

        String sql = "SELECT test_type_id, test_name, category, price, turnaround_time_hours, result_format "
                   + "FROM test_types "
                   + "ORDER BY test_name";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TestType test = new TestType(
                        rs.getInt("test_type_id"),
                        rs.getString("test_name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getInt("turnaround_time_hours"),
                        rs.getString("result_format")
                );

                tests.add(test);
            }

            rs.close();
            stmt.close();
            conn.close();

            testsTable.setItems(tests);

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Could not load tests from database.");
        }
    }

    @FXML
    private void submitTestRequest() {
        TestType selectedTest = testsTable.getSelectionModel().getSelectedItem();

        if (selectedTest == null) {
            messageLabel.setText("Please select a test first.");
            return;
        }

        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageLabel.setText("No customer is currently logged in.");
            return;
        }

        String sql = "INSERT INTO test_requests "
                   + "(customer_id, test_type_id, payment_status, request_status) "
                   + "VALUES (?, ?, 'UNPAID', 'PENDING_PAYMENT')";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, currentUser.getUserId());
            stmt.setInt(2, selectedTest.getTestTypeId());

            stmt.executeUpdate();

            stmt.close();
            conn.close();

            messageLabel.setText(
                    "Request submitted successfully. Bank details: Sante Diagnostics, Account No: 1234567890"
            );

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Could not submit test request.");
        }
    }

    @FXML
    private void backToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/customer_dashboard.fxml",
                "Customer Dashboard"
        );
    }
    
    @FXML
    private void openTestCatalogue(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/test_catalogue.fxml",
            "Available Tests"
        );
    }

    @FXML
    private void openMyRequests(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/my_requests.fxml",
            "My Test Requests"
        );
    }

    @FXML
    private void openResultVault(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/result_vault.fxml",
            "Result Vault"
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