package trioparadadura.example.manedeliveryfx;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.CategoriaDal;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Categoria;
import trioparadadura.example.manedeliveryfx.db.entidades.Marca;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;

import java.net.URL;
import java.util.List;
import java.util.Optional;
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
    void onNovoProduto(ActionEvent event) throws Exception {
        abrirFormulario(null);
    }

    @FXML
    void onAlterarProduto(ActionEvent event) throws Exception {
        Produto produto = tableView.getSelectionModel().getSelectedItem();
        if (produto != null)
            abrirFormulario(produto);
    }

    @FXML
    void onApagarProduto(ActionEvent event) {
        Produto produto = tableView.getSelectionModel().getSelectedItem();
        if (produto != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setContentText("Apagar o produto " + produto.getNome() + "?");
            if (alert.showAndWait().get() == ButtonType.OK) {
                if (new ProdutoDal().apagar(produto))
                    carregarTabela();
                else {
                    Alert erro = new Alert(Alert.AlertType.ERROR);
                    erro.setHeaderText("Não foi possível apagar");
                    erro.setContentText("Este produto já está em algum pedido.");
                    erro.showAndWait();
                }
            }
        }
    }

    private void abrirFormulario(Produto produto) throws Exception {
        FXMLLoader loader = new FXMLLoader(ManeDeliveryApplication.class.getResource("produto-form-view.fxml"));
        Scene scene = new Scene(loader.load());
        if (produto != null) {
            ProdutoFormController controller = loader.getController();
            controller.setProduto(produto);
        }
        Stage stage = new Stage();
        stage.setTitle(produto == null ? "Novo Produto" : "Alterar Produto");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
        carregarTabela();
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

