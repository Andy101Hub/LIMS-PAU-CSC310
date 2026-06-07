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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.PasswordResetRequest;
import lims.utils.BCryptUtil;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class PasswordResetRequestsController {

    @FXML
    private TableView<PasswordResetRequest> resetTable;

    @FXML
    private TableColumn<PasswordResetRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<PasswordResetRequest, String> emailColumn;

    @FXML
    private TableColumn<PasswordResetRequest, String> roleColumn;

    @FXML
    private TableColumn<PasswordResetRequest, String> statusColumn;

    @FXML
    private TableColumn<PasswordResetRequest, String> requestedAtColumn;

    @FXML
    private PasswordField temporaryPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("resetRequestId"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestedAtColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAt"));

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        ObservableList<PasswordResetRequest> requests = FXCollections.observableArrayList();

        String sql = "SELECT reset_request_id, user_id, email, role, status, requested_at "
                   + "FROM password_reset_requests "
                   + "WHERE status = 'PENDING' "
                   + "ORDER BY requested_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PasswordResetRequest request = new PasswordResetRequest(
                        rs.getInt("reset_request_id"),
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("role"),
                        rs.getString("status"),
                        rs.getString("requested_at")
                );

                requests.add(request);
            }

            resetTable.setItems(requests);

            if (requests.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #6b7280;");
                messageLabel.setText("No pending password reset requests.");
            } else {
                messageLabel.setText("");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not load password reset requests.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        messageLabel.setText("");
        loadPendingRequests();
    }

    @FXML
    private void handleSetTemporaryPassword() {
        PasswordResetRequest selectedRequest = resetTable.getSelectionModel().getSelectedItem();
        String temporaryPassword = temporaryPasswordField.getText();

        if (selectedRequest == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a reset request first.");
            return;
        }

        if (temporaryPassword.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please enter a temporary password.");
            return;
        }

        if (temporaryPassword.length() < 6) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Temporary password must be at least 6 characters.");
            return;
        }

        try {
            String hashedPassword = BCryptUtil.hashPassword(temporaryPassword);

            setTemporaryPassword(
                    selectedRequest.getUserId(),
                    selectedRequest.getResetRequestId(),
                    hashedPassword
            );

            insertAuditLog(
                    "ADMIN_RESET_LAB_PASSWORD",
                    "Super Admin issued a temporary password for Lab Attendant account: "
                    + selectedRequest.getEmail()
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Temporary password saved. Lab Attendant must change it on next login.");

            temporaryPasswordField.clear();
            loadPendingRequests();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not set temporary password.");
            e.printStackTrace();
        }
    }

    private void setTemporaryPassword(int userId,
                                      int resetRequestId,
                                      String hashedPassword) throws SQLException {

        Connection conn = DBConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            String updateUserSql = "UPDATE users "
                                 + "SET password_hash = ?, force_password_change = TRUE "
                                 + "WHERE user_id = ? AND role = 'LAB_ATTENDANT'";

            PreparedStatement userStmt = conn.prepareStatement(updateUserSql);
            userStmt.setString(1, hashedPassword);
            userStmt.setInt(2, userId);
            userStmt.executeUpdate();
            userStmt.close();

            String updateRequestSql = "UPDATE password_reset_requests "
                                    + "SET status = 'HANDLED', "
                                    + "handled_by = ?, "
                                    + "handled_at = CURRENT_TIMESTAMP "
                                    + "WHERE reset_request_id = ?";

            PreparedStatement requestStmt = conn.prepareStatement(updateRequestSql);
            requestStmt.setInt(1, SessionManager.getCurrentUserId());
            requestStmt.setInt(2, resetRequestId);
            requestStmt.executeUpdate();
            requestStmt.close();

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;

        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private void insertAuditLog(String action, String details) throws SQLException {
        String sql = "INSERT INTO audit_logs (user_id, action, details) "
                   + "VALUES (?, ?, ?)";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, SessionManager.getCurrentUserId());
        stmt.setString(2, action);
        stmt.setString(3, details);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
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
    private void openPasswordResetRequests(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/password_reset_requests.fxml",
                "Password Reset Requests"
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