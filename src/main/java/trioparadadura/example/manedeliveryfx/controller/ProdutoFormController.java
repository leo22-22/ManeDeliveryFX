package trioparadadura.example.manedeliveryfx.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.CategoriaDal;
import trioparadadura.example.manedeliveryfx.db.dals.MarcaDal;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Categoria;
import trioparadadura.example.manedeliveryfx.db.entidades.Marca;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;
import trioparadadura.example.manedeliveryfx.util.Formato;
import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
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
        MaskFieldUtil.maxField(tfNome, 30);
        MaskFieldUtil.monetaryField(tfPreco);
        MaskFieldUtil.numericField(tfVolume);
        MaskFieldUtil.maxField(tfVolume, 6);
        MaskFieldUtil.numericField(tfEstoque);
        MaskFieldUtil.maxField(tfEstoque, 6);

        cbCategoria.setItems(FXCollections.observableArrayList(new CategoriaDal().get("")));
        cbMarca.setItems(FXCollections.observableArrayList(new MarcaDal().get("")));
    }

    // chamado pela tabela quando for alterar
    public void setProduto(Produto produto) {
        this.produto = produto;
        tfNome.setText(produto.getNome());
        tfPreco.setText(Formato.valor(produto.getPreco())); // "8,50" para a máscara monetária
        tfVolume.setText("" + produto.getVolume());
        tfEstoque.setText("" + produto.getEstoque());

        List<Categoria> categorias = cbCategoria.getItems();
        for (int i = 0; i < categorias.size(); i++)
            if (categorias.get(i).getId() == produto.getCategoria().getId())
                cbCategoria.setValue(categorias.get(i));
        List<Marca> marcas = cbMarca.getItems();
        for (int i = 0; i < marcas.size(); i++)
            if (marcas.get(i).getId() == produto.getMarca().getId())
                cbMarca.setValue(marcas.get(i));
    }

    @FXML
    void onSalvar(ActionEvent event) {
        String erro = validar();
        if (!erro.isEmpty())
            Mensagem.erro(erro);
        else {
            String nome = tfNome.getText().trim();
            double preco = MaskFieldUtil.monetaryValueFromField(tfPreco).doubleValue();
            int volume = Integer.parseInt(tfVolume.getText());
            int estoque = Integer.parseInt(tfEstoque.getText());

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
                Mensagem.erro("Erro ao salvar no banco de dados.");
        }
    }

    @FXML
    void onCancelar(ActionEvent event) {
        fechar();
    }

    // As máscaras já garantem só números em volume e estoque
    private String validar() {
        String erro = "";
        if (tfNome.getText().isBlank())
            erro += "Informe o nome.\n";
        BigDecimal preco = MaskFieldUtil.monetaryValueFromField(tfPreco);
        if (preco == null || preco.doubleValue() <= 0)
            erro += "Informe um preço maior que zero.\n";
        if (tfVolume.getText().isEmpty() || Integer.parseInt(tfVolume.getText()) <= 0)
            erro += "Informe o volume em ml.\n";
        if (tfEstoque.getText().isEmpty())
            erro += "Informe o estoque.\n";
        if (cbCategoria.getValue() == null)
            erro += "Selecione a categoria.\n";
        if (cbMarca.getValue() == null)
            erro += "Selecione a marca.\n";
        return erro;
    }

    private void fechar() {
        ((Stage) tfNome.getScene().getWindow()).close();
    }
}
