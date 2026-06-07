package lims.controllers;

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
import javafx.stage.Stage;

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

        // Temporary demo row until Person 1/2 creates the real results table.
        // Real result loading should filter WHERE status = 'VALIDATED'.
        results.add(new ResultItem(
                "No validated result yet",
                "VALIDATED ONLY",
                "PDF available here",
                "Image available here"
        ));

        resultsTable.setItems(results);
        messageLabel.setText("Only validated laboratory results are shown in this vault.");
    }

    @FXML
    private void viewPdfReport(ActionEvent event) {
        showInfo("PDF Report", "PDF report viewing feature is available from the Result Vault.");
    }

    @FXML
    private void downloadPdfReport(ActionEvent event) {
        showInfo("Download PDF", "PDF report download feature is available from the Result Vault.");
    }

    @FXML
    private void viewMedicalImage(ActionEvent event) {
        showInfo("Medical Image", "Medical image viewing feature is available from the Result Vault.");
    }

    @FXML
    private void downloadMedicalImage(ActionEvent event) {
        showInfo("Download Image", "Medical image download feature is available from the Result Vault.");
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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