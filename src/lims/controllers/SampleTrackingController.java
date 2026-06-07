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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.TestRequest;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class SampleTrackingController {

    @FXML
    private TableView<TestRequest> requestTable;

    @FXML
    private TableColumn<TestRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<TestRequest, String> customerNameColumn;

    @FXML
    private TableColumn<TestRequest, String> customerEmailColumn;

    @FXML
    private TableColumn<TestRequest, String> testNameColumn;

    @FXML
    private TableColumn<TestRequest, String> paymentStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> requestStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> requestedAtColumn;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        requestedAtColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAt"));

        statusComboBox.setItems(FXCollections.observableArrayList(
                "PAID",
                "SAMPLE_COLLECTED",
                "PROCESSING",
                "AWAITING_VALIDATION",
                "VALIDATED",
                "COMPLETED"
        ));

        loadRequests();
    }

    private void loadRequests() {
        ObservableList<TestRequest> requests = FXCollections.observableArrayList();

        String sql = "SELECT tr.request_id, u.full_name, u.email, tt.test_name, "
                   + "tr.payment_status, tr.request_status, tr.requested_at "
                   + "FROM test_requests tr "
                   + "JOIN users u ON tr.customer_id = u.user_id "
                   + "JOIN test_types tt ON tr.test_type_id = tt.test_type_id "
                   + "WHERE tr.payment_status = 'PAID' "
                   + "ORDER BY tr.requested_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TestRequest request = new TestRequest(
                        rs.getInt("request_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("test_name"),
                        rs.getString("payment_status"),
                        rs.getString("request_status"),
                        rs.getString("requested_at")
                );

                requests.add(request);
            }

            requestTable.setItems(requests);

            if (requests.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #6b7280;");
                messageLabel.setText("No paid requests available for sample tracking.");
            } else {
                messageLabel.setText("");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not load sample tracking requests.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        messageLabel.setText("");
        loadRequests();
    }

    @FXML
    private void handleUpdateStatus() {
        TestRequest selectedRequest = requestTable.getSelectionModel().getSelectedItem();
        String selectedStatus = statusComboBox.getValue();

        if (selectedRequest == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a request first.");
            return;
        }

        if (selectedStatus == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a new sample status.");
            return;
        }

        try {
            updateRequestStatus(selectedRequest.getRequestId(), selectedStatus);

            insertAuditLog(
                    "SAMPLE_STATUS_UPDATE",
                    "Updated request ID "
                    + selectedRequest.getRequestId()
                    + " to status "
                    + selectedStatus
                    + "."
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Sample status updated successfully.");

            statusComboBox.setValue(null);
            loadRequests();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not update sample status.");
            e.printStackTrace();
        }
    }

    private void updateRequestStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE test_requests "
                   + "SET request_status = ?, updated_at = CURRENT_TIMESTAMP "
                   + "WHERE request_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, status);
        stmt.setInt(2, requestId);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
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
    private void backToDashboard(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/lab_attendant_dashboard.fxml",
                "Lab Attendant Dashboard"
        );
    }

    @FXML
    private void openLabRequestQueue(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/lab_test_request_queue.fxml",
                "Lab Test Request Queue"
        );
    }

    @FXML
    private void openSampleTracking(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/sample_tracking.fxml",
                "Sample Lifecycle Tracking"
        );
    }

    @FXML
    private void openResultUploadValidation(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
                stage,
                "/lims/views/result_upload_validation.fxml",
                "Result Upload & Validation"
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
    private void openCustomerProfiles(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        SceneNavigator.switchScene(
            stage,
            "/lims/views/lab_customer_profiles.fxml",
            "Customer Profiles"
        );
    }
}