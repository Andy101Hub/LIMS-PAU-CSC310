package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.AuditLog;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class AuditTrailController {

    @FXML
    private TableView<AuditLog> auditTable;

    @FXML
    private TableColumn<AuditLog, Integer> auditIdColumn;

    @FXML
    private TableColumn<AuditLog, String> userNameColumn;

    @FXML
    private TableColumn<AuditLog, String> userEmailColumn;

    @FXML
    private TableColumn<AuditLog, String> actionColumn;

    @FXML
    private TableColumn<AuditLog, String> detailsColumn;

    @FXML
    private TableColumn<AuditLog, String> createdAtColumn;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        auditIdColumn.setCellValueFactory(new PropertyValueFactory<>("auditId"));
        userNameColumn.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("userEmail"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        loadAuditLogs();
    }

    private void loadAuditLogs() {
        ObservableList<AuditLog> logs = FXCollections.observableArrayList();

        String sql = "SELECT a.audit_id, u.full_name, u.email, a.action, a.details, a.created_at "
                   + "FROM audit_logs a "
                   + "LEFT JOIN users u ON a.user_id = u.user_id "
                   + "ORDER BY a.created_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AuditLog log = new AuditLog(
                        rs.getInt("audit_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("action"),
                        rs.getString("details"),
                        rs.getString("created_at")
                );

                logs.add(log);
            }

            auditTable.setItems(logs);

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not load audit logs.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        messageLabel.setText("");
        loadAuditLogs();
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