package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.User;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class CustomerDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalRequestsLabel;

    @FXML
    private Label inProgressLabel;

    @FXML
    private Label readyResultsLabel;

    @FXML
    private Label unpaidLabel;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            loadCustomerDashboardCounts(currentUser.getUserId());
        }
    }

    private void loadCustomerDashboardCounts(int customerId) {
        totalRequestsLabel.setText(String.valueOf(getCustomerCount(
                "SELECT COUNT(*) FROM test_requests WHERE customer_id = ?",
                customerId
        )));

        inProgressLabel.setText(String.valueOf(getCustomerCount(
                "SELECT COUNT(*) FROM test_requests "
                + "WHERE customer_id = ? "
                + "AND request_status IN ('PAID', 'SAMPLE_COLLECTED', 'PROCESSING', 'AWAITING_VALIDATION')",
                customerId
        )));

        readyResultsLabel.setText(String.valueOf(getCustomerCount(
                "SELECT COUNT(*) FROM test_requests "
                + "WHERE customer_id = ? "
                + "AND result_status = 'VALIDATED'",
                customerId
        )));

        unpaidLabel.setText(String.valueOf(getCustomerCount(
                "SELECT COUNT(*) FROM test_requests "
                + "WHERE customer_id = ? "
                + "AND payment_status = 'UNPAID'",
                customerId
        )));
    }

    private int getCustomerCount(String sql, int customerId) {
        int count = 0;

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, customerId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
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