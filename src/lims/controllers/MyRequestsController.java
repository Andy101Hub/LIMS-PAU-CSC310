package lims.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

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
import lims.models.TestRequest;
import lims.models.User;
import lims.utils.SceneNavigator;
import lims.utils.SessionManager;

public class MyRequestsController {

    @FXML
    private TableView<TestRequest> requestsTable;

    @FXML
    private TableColumn<TestRequest, Integer> requestIdColumn;

    @FXML
    private TableColumn<TestRequest, String> testNameColumn;

    @FXML
    private TableColumn<TestRequest, String> paymentStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> requestStatusColumn;

    @FXML
    private TableColumn<TestRequest, String> requestedAtColumn;

    @FXML
    private TableColumn<TestRequest, String> countdownColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("requestStatus"));
        requestedAtColumn.setCellValueFactory(new PropertyValueFactory<>("requestedAt"));
        countdownColumn.setCellValueFactory(new PropertyValueFactory<>("countdown"));

        loadMyRequests();
    }

    private void loadMyRequests() {
        User currentUser = SessionManager.getCurrentUser();

        if (currentUser == null) {
            messageLabel.setText("No customer is currently logged in.");
            return;
        }

        ObservableList<TestRequest> requests = FXCollections.observableArrayList();

        String sql = "SELECT tr.request_id, u.full_name, u.email, tt.test_name, "
                   + "tr.payment_status, tr.request_status, tr.requested_at, "
                   + "tt.turnaround_time_hours "
                   + "FROM test_requests tr "
                   + "JOIN users u ON tr.customer_id = u.user_id "
                   + "JOIN test_types tt ON tr.test_type_id = tt.test_type_id "
                   + "WHERE tr.customer_id = ? "
                   + "ORDER BY tr.requested_at DESC";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, currentUser.getUserId());

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp requestedTimestamp = rs.getTimestamp("requested_at");
                int turnaroundHours = rs.getInt("turnaround_time_hours");

                String requestedAtText = requestedTimestamp.toLocalDateTime().toString();
                String countdownText = calculateCountdown(
                        requestedTimestamp.toLocalDateTime(),
                        turnaroundHours,
                        rs.getString("request_status")
                );

                TestRequest request = new TestRequest(
                        rs.getInt("request_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("test_name"),
                        rs.getString("payment_status"),
                        rs.getString("request_status"),
                        requestedAtText,
                        turnaroundHours,
                        countdownText
                );

                requests.add(request);
            }

            rs.close();
            stmt.close();
            conn.close();

            requestsTable.setItems(requests);

            if (requests.isEmpty()) {
                messageLabel.setText("You have no test requests yet.");
            } else {
                messageLabel.setText("Countdown is calculated using requested time plus turnaround hours.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Could not load your requests.");
        }
    }

    private String calculateCountdown(LocalDateTime requestedAt, int turnaroundHours, String requestStatus) {
        if (requestStatus == null) {
            return "Unknown";
        }

        if (requestStatus.equalsIgnoreCase("COMPLETED")
                || requestStatus.equalsIgnoreCase("VALIDATED")
                || requestStatus.equalsIgnoreCase("RELEASED")) {
            return "Ready";
        }

        LocalDateTime readyTime = requestedAt.plusHours(turnaroundHours);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(readyTime)) {
            return "Due now";
        }

        Duration remaining = Duration.between(now, readyTime);

        long hours = remaining.toHours();
        long minutes = remaining.toMinutes() % 60;

        return hours + "h " + minutes + "m remaining";
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
}