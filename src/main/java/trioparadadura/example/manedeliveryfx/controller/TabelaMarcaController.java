package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import trioparadadura.example.manedeliveryfx.db.dals.MarcaDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Marca;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TabelaMarcaController implements Initializable {

    @FXML
    private TableColumn<Marca, Integer> coId;

    @FXML
    private TableColumn<Marca, String> coNome;

    @FXML
    private TableView<Marca> tableView;

    @FXML
    void onNovaMarca(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        MaskFieldUtil.maxField(dialog.getEditor(), 20);
        dialog.setHeaderText("Nova marca");
        dialog.setContentText("Nome:");
        Optional<String> nome = dialog.showAndWait();
        if (nome.isPresent() && !nome.get().isBlank()) {
            new MarcaDal().gravar(new Marca(nome.get().trim()));
            carregarTabela();
        }
    }

    @FXML
    void onApagarMarca(ActionEvent event) {
        Marca marca = tableView.getSelectionModel().getSelectedItem();
        if (marca == null)
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Apagar a marca " + marca.getNome() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (new MarcaDal().apagar(marca))
                carregarTabela();
            else {
                Alert erro = new Alert(Alert.AlertType.ERROR);
                erro.setHeaderText("Não foi possível apagar");
                erro.setContentText("Existem produtos usando este registro.");
                erro.showAndWait();
            }
        }
    }

    @FXML
    void onAlterarMarca(ActionEvent event) {
        Marca marca = tableView.getSelectionModel().getSelectedItem();
        if (marca == null)
            return;
        TextInputDialog dialog = new TextInputDialog(marca.getNome());
        MaskFieldUtil.maxField(dialog.getEditor(), 20);
        dialog.setHeaderText("Alterar marca");
        dialog.setContentText("Nome:");
        Optional<String> nome = dialog.showAndWait();
        if (nome.isPresent() && !nome.get().isBlank()) {
            marca.setNome(nome.get().trim());
            new MarcaDal().alterar(marca);
            carregarTabela();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        carregarTabela();
    }

    private void carregarTabela() {
        MarcaDal marcaDal = new MarcaDal();
        List<Marca> marcaList = marcaDal.get("");
        tableView.setItems(FXCollections.observableArrayList(marcaList));
    }
}
