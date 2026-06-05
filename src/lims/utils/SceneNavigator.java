package lims.utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneNavigator {

    public static void switchScene(Stage stage, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(SceneNavigator.class.getResource(fxmlPath));

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                    SceneNavigator.class.getResource("/lims/styles/style.css").toExternalForm()
            );

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}