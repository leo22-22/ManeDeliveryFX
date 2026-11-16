package trioparadadura.example.manedeliveryfx;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import trioparadadura.example.manedeliveryfx.db.dals.CategoriaDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Categoria;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TabelaCategoriaController implements Initializable {

    @FXML
    private TableColumn<Categoria, Integer> coId;

    @FXML
    private TableColumn<Categoria, String> coNome;

    @FXML
    private TableView<Categoria> tableView;

    @FXML
    void onNovaCategoria(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Nova categoria");
        dialog.setContentText("Nome:");
        Optional<String> nome = dialog.showAndWait();
        if (nome.isPresent() && !nome.get().isBlank()) {
            new CategoriaDal().gravar(new Categoria(nome.get().trim()));
            carregarTabela();
        }
    }

    @FXML
    void onApagarCategoria(ActionEvent event) {
        Categoria categoria = tableView.getSelectionModel().getSelectedItem();
        if (categoria == null)
            return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Apagar a categoria " + categoria.getNome() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            if (new CategoriaDal().apagar(categoria))
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
    void onAlterarCategoria(ActionEvent event) {
        Categoria categoria = tableView.getSelectionModel().getSelectedItem();
        if (categoria == null)
            return;
        TextInputDialog dialog = new TextInputDialog(categoria.getNome());
        dialog.setHeaderText("Alterar categoria");
        dialog.setContentText("Nome:");
        Optional<String> nome = dialog.showAndWait();
        if (nome.isPresent() && !nome.get().isBlank()) {
            categoria.setNome(nome.get().trim());
            new CategoriaDal().alterar(categoria);
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
        CategoriaDal categoriaDal = new CategoriaDal();
        List<Categoria> categoriaList = categoriaDal.get("");
        tableView.setItems(FXCollections.observableArrayList(categoriaList));
    }
}
