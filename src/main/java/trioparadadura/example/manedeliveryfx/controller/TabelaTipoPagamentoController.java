package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import trioparadadura.example.manedeliveryfx.db.dals.TipoPagamentoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.TipoPagamento;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TabelaTipoPagamentoController implements Initializable {

    @FXML
    private TableColumn<TipoPagamento, Integer> coId;

    @FXML
    private TableColumn<TipoPagamento, String> coNome;

    @FXML
    private TableView<TipoPagamento> tableView;

    @FXML
    void onNovoTipoPagamento(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        MaskFieldUtil.maxField(dialog.getEditor(), 20);
        dialog.setHeaderText("Novo tipo de pagamento");
        dialog.setContentText("Nome:");
        Optional<String> nome = dialog.showAndWait();
        if (nome.isPresent() && !nome.get().isBlank()) {
            new TipoPagamentoDal().gravar(new TipoPagamento(nome.get().trim()));
            carregarTabela();
        }
    }

    @FXML
    void onApagarTipoPagamento(ActionEvent event) {
        TipoPagamento tipoPagamento = tableView.getSelectionModel().getSelectedItem();
        if (tipoPagamento != null) {
            if (Mensagem.confirmar("Apagar o tipo de pagamento " + tipoPagamento.getNome() + "?")) {
                if (new TipoPagamentoDal().apagar(tipoPagamento))
                    carregarTabela();
                else
                    Mensagem.erro("Existem pedidos usando este tipo de pagamento.");
            }
        }
    }

    @FXML
    void onAlterarTipoPagamento(ActionEvent event) {
        TipoPagamento tipoPagamento = tableView.getSelectionModel().getSelectedItem();
        if (tipoPagamento != null) {
            TextInputDialog dialog = new TextInputDialog(tipoPagamento.getNome());
            MaskFieldUtil.maxField(dialog.getEditor(), 20);
            dialog.setHeaderText("Alterar tipo de pagamento");
            dialog.setContentText("Nome:");
            Optional<String> nome = dialog.showAndWait();
            if (nome.isPresent() && !nome.get().isBlank()) {
                tipoPagamento.setNome(nome.get().trim());
                new TipoPagamentoDal().alterar(tipoPagamento);
                carregarTabela();
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        carregarTabela();
    }

    private void carregarTabela() {
        List<TipoPagamento> lista = new TipoPagamentoDal().get("");
        tableView.setItems(FXCollections.observableArrayList(lista));
    }
}
