package trioparadadura.example.manedeliveryfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.util.SingletonDB;

import java.io.IOException;

public class ManeDeliveryApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        if(SingletonDB.conectar()){
            FXMLLoader fxmlLoader = new FXMLLoader(ManeDeliveryApplication.class.getResource("principal-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("ManeDeliveryFX");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        }else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro ao conectar no banco");
            alert.setContentText(SingletonDB.getConexao().getMensagemErro());
            alert.showAndWait();
            Platform.exit();
        }
    }
}
