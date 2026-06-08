package lims.controllers;

import java.io.File;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.TestRequest;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;
import lims.utils.EmailService;

public class ResultUploadValidationController {

    @FXML private TableView<TestRequest> requestTable;
    @FXML private TableColumn<TestRequest, Integer> requestIdColumn;
    @FXML private TableColumn<TestRequest, String> customerNameColumn;
    @FXML private TableColumn<TestRequest, String> customerEmailColumn;
    @FXML private TableColumn<TestRequest, String> testNameColumn;
    @FXML private TableColumn<TestRequest, String> paymentStatusColumn;
    @FXML private TableColumn<TestRequest, String> requestStatusColumn;
    @FXML private TableColumn<TestRequest, String> requestedAtColumn;

    @FXML private Label pdfPathLabel;
    @FXML private Label imagePathLabel;
    @FXML private Label messageLabel;

    private File selectedPdfFile;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerEmailColumn.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        requestedAtColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAt"));

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
                + "AND tr.request_status IN ('PAID', 'AWAITING_VALIDATION', 'PROCESSING', 'SAMPLE_COLLECTED', 'VALIDATED', 'COMPLETED') "
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
                messageLabel.setText("No paid requests are currently ready for result upload.");
            } else {
                messageLabel.setText("");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not load requests for result upload.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChoosePdf(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose PDF Report");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedPdfFile = file;
            pdfPathLabel.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Medical Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imagePathLabel.setText(file.getAbsolutePath());
        }
    }

    @FXML
    private void handleUploadFiles() {
        TestRequest selectedRequest = requestTable.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a request first.");
            return;
        }

        if (selectedPdfFile == null && selectedImageFile == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please choose at least one result file to upload.");
            return;
        }

        String pdfPath = selectedPdfFile == null ? null : selectedPdfFile.getAbsolutePath();
        String imagePath = selectedImageFile == null ? null : selectedImageFile.getAbsolutePath();

        try {
            uploadResultFiles(selectedRequest.getRequestId(), pdfPath, imagePath);

            insertAuditLog(
                    "RESULT_FILES_UPLOADED",
                    "Uploaded result files for request ID " + selectedRequest.getRequestId() + "."
            );

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Result files uploaded successfully. You can now validate the result.");

            selectedPdfFile = null;
            selectedImageFile = null;
            pdfPathLabel.setText("No PDF selected");
            imagePathLabel.setText("No image selected");

            loadRequests();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not upload result files.");
            e.printStackTrace();
        }
    }

    private void uploadResultFiles(int requestId, String pdfPath, String imagePath) throws SQLException {
        String sql = "UPDATE test_requests "
                + "SET pdf_report_path = COALESCE(?, pdf_report_path), "
                + "medical_image_path = COALESCE(?, medical_image_path), "
                + "result_status = 'UPLOADED', "
                + "request_status = 'AWAITING_VALIDATION', "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE request_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setString(1, pdfPath);
        stmt.setString(2, imagePath);
        stmt.setInt(3, requestId);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    @FXML
    private void handleValidateResult() {
        TestRequest selectedRequest = requestTable.getSelectionModel().getSelectedItem();

        if (selectedRequest == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Please select a request first.");
            return;
        }

        try {
            if (!requestHasUploadedResult(selectedRequest.getRequestId())) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Please upload a PDF report or medical image before validation.");
                return;
            }

            validateResult(selectedRequest.getRequestId());

            insertAuditLog(
                    "RESULT_VALIDATED",
                    "Validated and released result for request ID "
                            + selectedRequest.getRequestId()
                            + "."
            );

            boolean emailSent = EmailService.sendResultReadyEmail(
                    selectedRequest.getCustomerEmail(),
                    selectedRequest.getCustomerName(),
                    selectedRequest.getTestName()
            );

            messageLabel.setStyle("-fx-text-fill: green;");

            if (emailSent) {
                messageLabel.setText("Result validated and released. Customer email notification sent.");
            } else {
                messageLabel.setText("Result validated and released, but email notification could not be sent.");
            }

            loadRequests();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not validate result.");
            e.printStackTrace();
        }
    }

    private boolean requestHasUploadedResult(int requestId) throws SQLException {
        String sql = "SELECT pdf_report_path, medical_image_path "
                + "FROM test_requests "
                + "WHERE request_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, requestId);

        ResultSet rs = stmt.executeQuery();

        boolean hasResult = false;

        if (rs.next()) {
            String pdfPath = rs.getString("pdf_report_path");
            String imagePath = rs.getString("medical_image_path");

            hasResult = (pdfPath != null && !pdfPath.isEmpty())
                    || (imagePath != null && !imagePath.isEmpty());
        }

        rs.close();
        stmt.close();
        conn.close();

        return hasResult;
    }

    private void validateResult(int requestId) throws SQLException {
        String sql = "UPDATE test_requests "
                + "SET result_status = 'VALIDATED', "
                + "request_status = 'VALIDATED', "
                + "validated_by = ?, "
                + "validated_at = CURRENT_TIMESTAMP, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE request_id = ?";

        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);

        stmt.setInt(1, SessionManager.getCurrentUserId());
        stmt.setInt(2, requestId);

        stmt.executeUpdate();

        stmt.close();
        conn.close();
    }

    @FXML
    private void handleRefresh() {
        messageLabel.setText("");
        loadRequests();
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