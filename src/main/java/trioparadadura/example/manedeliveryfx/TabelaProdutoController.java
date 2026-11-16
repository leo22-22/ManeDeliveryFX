package trioparadadura.example.manedeliveryfx;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Categoria;
import trioparadadura.example.manedeliveryfx.db.entidades.Marca;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class TabelaProdutoController implements Initializable {

    @FXML
    private TableColumn<Produto, Categoria> coCategoria;

    @FXML
    private TableColumn<Produto, Integer> coEstoque;

    @FXML
    private TableColumn<Produto, Integer> coId;

    @FXML
    private TableColumn<Produto, Marca> coMarca;

    @FXML
    private TableColumn<Produto, String> coNome;

    @FXML
    private TableColumn<Produto, Double> coPreco;

    @FXML
    private TableView<Produto> tableView;

    @FXML
    private TableColumn<Produto, Integer> coVolume;

    @FXML
    void onAlterarProduto(ActionEvent event) {

    }

    @FXML
    void onApagarProduto(ActionEvent event) {

    }

    @FXML
    void onNovoProduto(ActionEvent event) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        coCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        coEstoque.setCellValueFactory(new PropertyValueFactory<>("estoque"));
        coMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        coPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        coVolume.setCellValueFactory(new PropertyValueFactory<>("volume"));
        carregarTabela();
    }

    private void carregarTabela() {
        ProdutoDal produtoDal=new ProdutoDal();
        List<Produto> produtoList=produtoDal.get("");
        tableView.setItems(FXCollections.observableArrayList(produtoList));
    }

}

