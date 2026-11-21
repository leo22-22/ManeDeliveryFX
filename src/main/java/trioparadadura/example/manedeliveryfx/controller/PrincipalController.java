package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.ManeDeliveryApplication;
import trioparadadura.example.manedeliveryfx.service.Relatorios;
import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class PrincipalController {

    // ------------------------------------------------ Cadastros

    @FXML
    void onProduto(ActionEvent event) throws Exception {
        abrirJanela("tabela-produto-view.fxml", "Produtos Cadastrados");
    }

    @FXML
    void onCategoria(ActionEvent event) throws Exception {
        abrirJanela("tabela-categoria-view.fxml", "Categorias Cadastradas");
    }

    @FXML
    void onMarca(ActionEvent event) throws Exception {
        abrirJanela("tabela-marca-view.fxml", "Marcas Cadastradas");
    }

    @FXML
    void onTipoPagamento(ActionEvent event) throws Exception {
        abrirJanela("tabela-tipopagamento-view.fxml", "Tipos de Pagamento");
    }

    @FXML
    void onStatus(ActionEvent event) throws Exception {
        abrirJanela("tabela-status-view.fxml", "Status do Pedido");
    }

    @FXML
    void onFechar(ActionEvent event) {
        if (Mensagem.confirmar("Deseja fechar a aplicação?"))
            Platform.exit();
    }

    // ------------------------------------------------ Pedidos

    @FXML
    void onNovoPedido(ActionEvent event) throws Exception {
        abrirJanela("novo-pedido-view.fxml", "Novo Pedido");
    }

    @FXML
    void onGerenciar(ActionEvent event) throws Exception {
        abrirJanela("gerenciar-pedidos-view.fxml", "Gerenciar Pedidos");
    }

    // ------------------------------------------------ Relatórios

    @FXML
    void onListaPreco(ActionEvent event) {
        Relatorios.listaPrecos(null);
    }

    @FXML
    void onRelPedidos(ActionEvent event) {
        DatePicker dpInicio = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dpFim = new DatePicker(LocalDate.now());
        MaskFieldUtil.dateField(dpInicio.getEditor());
        MaskFieldUtil.dateField(dpFim.getEditor());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.add(new Label("De:"), 0, 0);
        grid.add(dpInicio, 1, 0);
        grid.add(new Label("Até:"), 0, 1);
        grid.add(dpFim, 1, 1);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Pedidos por data");
        dialog.setHeaderText("Informe o período do relatório");
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> resposta = dialog.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            if (dpInicio.getValue() == null || dpFim.getValue() == null)
                Mensagem.erro("Informe as duas datas.");
            else if (dpFim.getValue().isBefore(dpInicio.getValue()))
                Mensagem.erro("A data final não pode ser antes da inicial.");
            else
                Relatorios.pedidosPorData(dpInicio.getValue(), dpFim.getValue(), null);
        }
    }

    // ------------------------------------------------ Ajuda

    @FXML
    void onSobre(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("ManeDelivery 1.0");
        alert.setContentText("Desenvolvido por:\nAndré Lopes\nLeonardo Picolo\nFellype Luz");
        alert.showAndWait();
    }

    private void abrirJanela(String fxml, String titulo) throws Exception {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(ManeDeliveryApplication.class.getResource("view/" + fxml));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle(titulo);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
