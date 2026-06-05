package lims.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/lims/views/login.fxml"));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    getClass().getResource("/lims/styles/style.css").toExternalForm()
            );

            primaryStage.setTitle("LIMS - Laboratory Information Management System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}