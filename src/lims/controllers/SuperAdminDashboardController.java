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

public class SuperAdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalUsersLabel;

    @FXML
    private Label testTypesLabel;

    @FXML
    private Label totalRequestsLabel;

    @FXML
    private Label pendingRequestsLabel;

    @FXML
    private void initialize() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }

        loadDashboardCounts();
    }

    private void loadDashboardCounts() {
        totalUsersLabel.setText(String.valueOf(getCount("SELECT COUNT(*) FROM users")));
        testTypesLabel.setText(String.valueOf(getCount("SELECT COUNT(*) FROM test_types")));
        totalRequestsLabel.setText(String.valueOf(getCount("SELECT COUNT(*) FROM test_requests")));

        pendingRequestsLabel.setText(String.valueOf(
                getCount("SELECT COUNT(*) FROM test_requests WHERE request_status IN ('PENDING_PAYMENT', 'PAID', 'SAMPLE_COLLECTED', 'PROCESSING', 'AWAITING_VALIDATION')")
        ));
    }

    private int getCount(String sql) {
        int count = 0;

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
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
    
    @FXML
    private void openPasswordResetRequests(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/password_reset_requests.fxml",
            "Password Reset Requests"
        );
    }
}