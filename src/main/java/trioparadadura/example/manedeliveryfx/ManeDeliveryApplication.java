package trioparadadura.example.manedeliveryfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ManeDeliveryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ManeDeliveryApplication.class.getResource("principal-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("ManeDelivery");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
