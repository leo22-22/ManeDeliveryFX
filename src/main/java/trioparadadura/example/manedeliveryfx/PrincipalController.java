package trioparadadura.example.manedeliveryfx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PrincipalController {

    @FXML
    void onCategoria(ActionEvent event) throws Exception {
        abrirJanela("tabela-categoria-view.fxml", "Categorias Cadastradas");
    }

    @FXML
    void onFechar(ActionEvent event) {
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Deseja fechar a aplicação?");
        if(alert.showAndWait().get()== ButtonType.OK)
           Platform.exit();
    }

    @FXML
    void onGerenciar(ActionEvent event) {

    }

    @FXML
    void onListaPreco(ActionEvent event) {

    }

    @FXML
    void onMarca(ActionEvent event) throws Exception {
        abrirJanela("tabela-marca-view.fxml", "Marcas Cadastradas");
    }

    @FXML
    void onNovoPedido(ActionEvent event) {

    }

    @FXML
    void onProduto(ActionEvent event) throws Exception {
        abrirJanela("tabela-produto-view.fxml", "Produtos Cadastrados");
    }

    private void abrirJanela(String fxml, String titulo) throws Exception {
        Stage stage=new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(ManeDeliveryApplication.class.getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(titulo);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    @FXML
    void onRelPedidos(ActionEvent event) {

    }

    @FXML
    void onSobre(ActionEvent event) {
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("ManeDalivery 1.0");
        alert.setContentText("Desenvolvido por:\nZé\nJosiany\nClaudemiro");
        alert.showAndWait();
    }

    @FXML
    void onStatus(ActionEvent event) {

    }

    @FXML
    void onTipoPagamento(ActionEvent event) {

    }

}
