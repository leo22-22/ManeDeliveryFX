package trioparadadura.example.manedeliveryfx;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.CategoriaDal;
import trioparadadura.example.manedeliveryfx.db.dals.MarcaDal;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Categoria;
import trioparadadura.example.manedeliveryfx.db.entidades.Marca;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;

import java.net.URL;
import java.util.ResourceBundle;

public class ProdutoFormController implements Initializable {

    @FXML
    private TextField tfNome;
    @FXML
    private TextField tfPreco;
    @FXML
    private TextField tfVolume;
    @FXML
    private TextField tfEstoque;
    @FXML
    private ComboBox<Categoria> cbCategoria;
    @FXML
    private ComboBox<Marca> cbMarca;

    private Produto produto = null; // null = novo produto

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbCategoria.setItems(FXCollections.observableArrayList(new CategoriaDal().get("")));
        cbMarca.setItems(FXCollections.observableArrayList(new MarcaDal().get("")));
    }

    // chamado pela tabela quando for alterar
    public void setProduto(Produto produto) {
        this.produto = produto;
        tfNome.setText(produto.getNome());
        tfPreco.setText(String.valueOf(produto.getPreco()));
        tfVolume.setText(String.valueOf(produto.getVolume()));
        tfEstoque.setText(String.valueOf(produto.getEstoque()));
        for (Categoria c : cbCategoria.getItems())
            if (c.getId() == produto.getCategoria().getId())
                cbCategoria.setValue(c);
        for (Marca m : cbMarca.getItems())
            if (m.getId() == produto.getMarca().getId())
                cbMarca.setValue(m);
    }

    @FXML
    void onSalvar(ActionEvent event) {
        String erro = validar();
        if (!erro.isEmpty())
            mostrarErro(erro);
        else {
            String nome = tfNome.getText().trim();
            double preco = Double.parseDouble(tfPreco.getText().trim().replace(",", "."));
            int volume = Integer.parseInt(tfVolume.getText().trim());
            int estoque = Integer.parseInt(tfEstoque.getText().trim());

            boolean ok;
            if (produto == null)
                ok = new ProdutoDal().gravar(new Produto(0, nome, preco, volume, estoque, cbCategoria.getValue(), cbMarca.getValue()));
            else {
                produto.setNome(nome);
                produto.setPreco(preco);
                produto.setVolume(volume);
                produto.setEstoque(estoque);
                produto.setCategoria(cbCategoria.getValue());
                produto.setMarca(cbMarca.getValue());
                ok = new ProdutoDal().alterar(produto);
            }

            if (ok)
                fechar();
            else
                mostrarErro("Erro ao salvar no banco de dados.");
        }
    }

    @FXML
    void onCancelar(ActionEvent event) {
        fechar();
    }

    private String validar() {
        String erro = "";
        if (tfNome.getText().isBlank())
            erro += "Informe o nome.\n";
        try {
            if (Double.parseDouble(tfPreco.getText().trim().replace(",", ".")) <= 0)
                erro += "O preço deve ser maior que zero.\n";
        } catch (NumberFormatException e) {
            erro += "Preço inválido.\n";
        }
        try {
            if (Integer.parseInt(tfVolume.getText().trim()) <= 0)
                erro += "O volume deve ser maior que zero.\n";
        } catch (NumberFormatException e) {
            erro += "Volume inválido (use só números, em ml).\n";
        }
        try {
            if (Integer.parseInt(tfEstoque.getText().trim()) < 0)
                erro += "O estoque não pode ser negativo.\n";
        } catch (NumberFormatException e) {
            erro += "Estoque inválido.\n";
        }
        if (cbCategoria.getValue() == null)
            erro += "Selecione a categoria.\n";
        if (cbMarca.getValue() == null)
            erro += "Selecione a marca.\n";
        return erro;
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Verifique os dados");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void fechar() {
        ((Stage) tfNome.getScene().getWindow()).close();
    }
}