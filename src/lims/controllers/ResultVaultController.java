package lims.controllers;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import lims.db.DBConnection;
import lims.models.ResultItem;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class ResultVaultController {

    @FXML
    private TableView<ResultItem> resultsTable;

    @FXML
    private TableColumn<ResultItem, String> testNameColumn;

    @FXML
    private TableColumn<ResultItem, String> statusColumn;

    @FXML
    private TableColumn<ResultItem, String> pdfColumn;

    @FXML
    private TableColumn<ResultItem, String> imageColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        pdfColumn.setCellValueFactory(new PropertyValueFactory<>("pdfReport"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("medicalImage"));

        loadValidatedResults();
    }

    private void loadValidatedResults() {
        ObservableList<ResultItem> results = FXCollections.observableArrayList();

        if (SessionManager.getCurrentUser() == null) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("No logged-in customer found.");
            resultsTable.setItems(results);
            return;
        }

        String sql = "SELECT tt.test_name, tr.result_status, tr.pdf_report_path, tr.medical_image_path "
                   + "FROM test_requests tr "
                   + "JOIN test_types tt ON tr.test_type_id = tt.test_type_id "
                   + "WHERE tr.customer_id = ? "
                   + "AND tr.result_status = 'VALIDATED' "
                   + "ORDER BY tr.validated_at DESC NULLS LAST, tr.requested_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, SessionManager.getCurrentUserId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ResultItem item = new ResultItem(
                        rs.getString("test_name"),
                        rs.getString("result_status"),
                        rs.getString("pdf_report_path"),
                        rs.getString("medical_image_path")
                );

                results.add(item);
            }

            resultsTable.setItems(results);

            if (results.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #6b7280;");
                messageLabel.setText("No validated laboratory results are available yet.");
            } else {
                messageLabel.setStyle("-fx-text-fill: #374151;");
                messageLabel.setText("Only validated laboratory results are shown in this vault.");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Could not load validated results.");
            e.printStackTrace();
        }
    }

    @FXML
    private void viewPdfReport(ActionEvent event) {
        ResultItem selectedItem = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showError("Please select a result first.");
            return;
        }

        String pdfPath = selectedItem.getPdfReportPath();

        if (pdfPath == null || pdfPath.trim().isEmpty()) {
            showError("This result has no PDF report.");
            return;
        }

        openFile(pdfPath, "PDF report");
    }

    @FXML
    private void downloadPdfReport(ActionEvent event) {
        ResultItem selectedItem = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showError("Please select a result first.");
            return;
        }

        String pdfPath = selectedItem.getPdfReportPath();

        if (pdfPath == null || pdfPath.trim().isEmpty()) {
            showError("This result has no PDF report.");
            return;
        }

        saveFileCopy(pdfPath, event, "Save PDF Report");
    }

    @FXML
    private void viewMedicalImage(ActionEvent event) {
        ResultItem selectedItem = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showError("Please select a result first.");
            return;
        }

        String imagePath = selectedItem.getMedicalImagePath();

        if (imagePath == null || imagePath.trim().isEmpty()) {
            showError("This result has no medical image.");
            return;
        }

        openFile(imagePath, "medical image");
    }

    @FXML
    private void downloadMedicalImage(ActionEvent event) {
        ResultItem selectedItem = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showError("Please select a result first.");
            return;
        }

        String imagePath = selectedItem.getMedicalImagePath();

        if (imagePath == null || imagePath.trim().isEmpty()) {
            showError("This result has no medical image.");
            return;
        }

        saveFileCopy(imagePath, event, "Save Medical Image");
    }

    private void openFile(String filePath, String fileDescription) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                showError("The selected " + fileDescription + " file could not be found.");
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                showError("Desktop file opening is not supported on this system.");
                return;
            }

            Desktop.getDesktop().open(file);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not open the selected file.");
        }
    }

    private void saveFileCopy(String sourcePath, ActionEvent event, String dialogTitle) {
        try {
            File sourceFile = new File(sourcePath);

            if (!sourceFile.exists()) {
                showError("The selected file could not be found.");
                return;
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(dialogTitle);
            fileChooser.setInitialFileName(sourceFile.getName());

            File destinationFile = fileChooser.showSaveDialog(stage);

            if (destinationFile != null) {
                Files.copy(
                        sourceFile.toPath(),
                        destinationFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                showInfo("Download Complete", "File saved successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not save the selected file.");
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Result Vault");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}