package trioparadadura.example.manedeliveryfx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Mensagem {

    public static void erro(String texto) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Atenção");
        alert.setContentText(texto);
        alert.showAndWait();
    }

    public static void info(String texto) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        alert.showAndWait();
    }

    public static boolean confirmar(String texto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(texto);
        Optional<ButtonType> resposta = alert.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.OK;
    }
}
