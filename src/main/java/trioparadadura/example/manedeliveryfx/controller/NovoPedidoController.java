package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.service.Relatorios;
import trioparadadura.example.manedeliveryfx.service.ViaCep;
import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Formato;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.PedidoDal;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.dals.StatusDal;
import trioparadadura.example.manedeliveryfx.db.dals.TipoPagamentoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Pedido;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;
import trioparadadura.example.manedeliveryfx.db.entidades.Status;
import trioparadadura.example.manedeliveryfx.db.entidades.TipoPagamento;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NovoPedidoController implements Initializable {

    @FXML
    private TextField tfId;
    @FXML
    private TextField tfNome;
    @FXML
    private DatePicker dpData;
    @FXML
    private TextField tfCep;
    @FXML
    private TextField tfNumero;
    @FXML
    private TextField tfLocal;
    @FXML
    private TextField tfTelefone;
    @FXML
    private ComboBox<TipoPagamento> cbTipoPagamento;
    @FXML
    private Label lbTotal;
    @FXML
    private HBox hbAdicionar;
    @FXML
    private ComboBox<Produto> cbProduto;
    @FXML
    private Spinner<Integer> spQuantidade;
    @FXML
    private TableView<Pedido.Item> tvItens;
    @FXML
    private TableColumn<Pedido.Item, String> coProduto;
    @FXML
    private TableColumn<Pedido.Item, String> coQuantidade;
    @FXML
    private TableColumn<Pedido.Item, String> coPreco;
    @FXML
    private TableColumn<Pedido.Item, String> coTotal;
    @FXML
    private Button btConfirmar;
    @FXML
    private Button btCancelar;
    @FXML
    private Button btImprimir;

    private final ObservableList<Pedido.Item> itens = FXCollections.observableArrayList();
    private Pedido pedidoGravado = null;
    private String cepConsultado = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskFieldUtil.maxField(tfNome, 50);
        MaskFieldUtil.cepField(tfCep);
        MaskFieldUtil.maxField(tfNumero, 10);
        MaskFieldUtil.foneField(tfTelefone);
        MaskFieldUtil.dateField(dpData.getEditor());
        MaskFieldUtil.numericField(spQuantidade.getEditor());

        dpData.setValue(LocalDate.now());
        cbTipoPagamento.setItems(FXCollections.observableArrayList(new TipoPagamentoDal().get("")));
        // só aparecem produtos com estoque disponível
        cbProduto.setItems(FXCollections.observableArrayList(new ProdutoDal().get("pro_estoque > 0")));
        spQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9999, 1));

        coProduto.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().produto().getNome() + " - " + c.getValue().produto().getMarca()));
        coQuantidade.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().quantidade()));
        coPreco.setCellValueFactory(c -> new SimpleStringProperty(Formato.valor(c.getValue().produto().getPreco())));
        coTotal.setCellValueFactory(c -> new SimpleStringProperty(Formato.valor(c.getValue().valor())));
        tvItens.setItems(itens);

        // busca o CEP quando o campo perde o foco (o Enter também busca)
        tfCep.focusedProperty().addListener((obs, antes, agora) -> {
            if (!agora)
                buscarCep();
        });
    }

    // Usado pela tela Gerenciar Pedidos para mostrar um pedido já gravado
    public void setPedido(Pedido pedido) {
        pedidoGravado = pedido;
        tfId.setText("" + pedido.getId());
        tfNome.setText(pedido.getClienteNome());
        dpData.setValue(pedido.getData());
        tfTelefone.setText(pedido.getClienteTelefone());
        tfNumero.setText(pedido.getNumeroRes());
        tfLocal.setText(pedido.getLocal());
        Matcher m = Pattern.compile("CEP (\\d{5}-\\d{3})").matcher(pedido.getLocal());
        if (m.find())
            tfCep.setText(m.group(1));
        cepConsultado = tfCep.getText().replaceAll("\\D", "");
        if (pedido.getTipoPagamento() != null) {
            List<TipoPagamento> tipos = cbTipoPagamento.getItems();
            for (int i = 0; i < tipos.size(); i++)
                if (tipos.get(i).getId() == pedido.getTipoPagamento().getId())
                    cbTipoPagamento.setValue(tipos.get(i));
        }
        itens.setAll(pedido.getItens());
        lbTotal.setText(Formato.valor(pedido.getTotal()));
        bloquearEdicao();
        btConfirmar.setVisible(false);
        btConfirmar.setManaged(false);
    }

    @FXML
    void onBuscarCep(ActionEvent event) {
        buscarCep();
    }

    private void buscarCep() {
        String cep = tfCep.getText().replaceAll("\\D", "");
        if (!cep.equals(cepConsultado) && tfCep.isEditable()) {
            cepConsultado = cep;
            tfLocal.setText("");
            if (!cep.isEmpty()) {
                tfLocal.setPromptText("Buscando CEP...");
                // a consulta roda fora da tela para ela não travar
                Thread consulta = new Thread(() -> {
                    ViaCep viaCep = new ViaCep();
                    boolean achou = viaCep.buscar(cep);
                    Platform.runLater(() -> {
                        tfLocal.setPromptText("Preenchida automaticamente pelo CEP");
                        if (achou) {
                            tfLocal.setText(viaCep.getLocalizacao());
                            tfCep.setText(cep.substring(0, 5) + "-" + cep.substring(5));
                            cepConsultado = cep;
                        } else {
                            cepConsultado = "";
                            Mensagem.erro(viaCep.getErro());
                        }
                    });
                });
                consulta.setDaemon(true);
                consulta.start();
            }
        }
    }

    @FXML
    void onAdicionarItem(ActionEvent event) {
        Produto produto = cbProduto.getValue();
        int quantidade = lerQuantidade();
        if (produto == null)
            Mensagem.erro("Selecione um produto.");
        else if (quantidade <= 0)
            Mensagem.erro("Informe uma quantidade válida.");
        else {
            // se o produto já está no pedido, soma a quantidade em vez de repetir o item
            int posicao = posicaoDoProduto(produto.getId());
            int novaQuantidade = quantidade;
            if (posicao >= 0)
                novaQuantidade += itens.get(posicao).quantidade();

            if (novaQuantidade > produto.getEstoque())
                Mensagem.erro("Estoque insuficiente para " + produto.getNome() + ".\nDisponível: "
                        + produto.getEstoque() + " | No pedido: " + novaQuantidade);
            else {
                Pedido.Item item = new Pedido.Item(produto, novaQuantidade,
                        Formato.arredondar(novaQuantidade * produto.getPreco()));
                if (posicao >= 0)
                    itens.set(posicao, item);
                else
                    itens.add(item);
                atualizarTotal();
                spQuantidade.getValueFactory().setValue(1);
                cbProduto.setValue(null);
            }
        }
    }

    @FXML
    void onRemoverItem(ActionEvent event) {
        Pedido.Item item = tvItens.getSelectionModel().getSelectedItem();
        if (item == null)
            Mensagem.erro("Selecione na tabela o item que deseja remover.");
        else if (Mensagem.confirmar("Remover " + item.produto().getNome() + " do pedido?")) {
            itens.remove(item);
            atualizarTotal();
        }
    }

    @FXML
    void onConfirmar(ActionEvent event) {
        String erro = validar();
        if (!erro.isEmpty())
            Mensagem.erro(erro);
        else {
            String cep = tfCep.getText().replaceAll("\\D", "");
            String cepFormatado = cep.substring(0, 5) + "-" + cep.substring(5);
            Pedido pedido = new Pedido(
                    dpData.getValue(),
                    tfNome.getText().trim(),
                    tfTelefone.getText(),
                    calcularTotal(),
                    cbTipoPagamento.getValue(),
                    tfLocal.getText() + " - CEP " + cepFormatado,
                    tfNumero.getText().trim(),
                    statusPendente()
            );
            for (int i = 0; i < itens.size(); i++)
                pedido.getItens().add(itens.get(i));

            PedidoDal pedidoDal = new PedidoDal();
            if (pedidoDal.gravar(pedido)) {
                pedidoGravado = pedido;
                tfId.setText("" + pedido.getId());
                bloquearEdicao();
                btConfirmar.setDisable(true);
                Mensagem.info("Pedido nº " + pedido.getId() + " gravado com sucesso!");
            } else
                Mensagem.erro(pedidoDal.getErro());
        }
    }

    @FXML
    void onCancelar(ActionEvent event) {
        boolean fechar = true;
        if (pedidoGravado == null && (!itens.isEmpty() || !tfNome.getText().isBlank()))
            fechar = Mensagem.confirmar("Descartar este pedido?");
        if (fechar)
            ((Stage) tfNome.getScene().getWindow()).close();
    }

    @FXML
    void onImprimir(ActionEvent event) {
        if (pedidoGravado != null)
            Relatorios.pedido(pedidoGravado, tfNome.getScene().getWindow());
    }

    private String validar() {
        String erro = "";
        String nome = tfNome.getText().trim();
        String telefone = tfTelefone.getText().replaceAll("\\D", "");
        if (nome.isEmpty())
            erro += "Informe o nome do cliente.\n";
        else if (nome.length() > 50)
            erro += "O nome pode ter no máximo 50 caracteres.\n";
        if (telefone.length() < 10 || telefone.length() > 11)
            erro += "Informe o telefone com DDD (10 ou 11 números).\n";
        if (dpData.getValue() == null)
            erro += "Informe a data do pedido.\n";
        if (tfLocal.getText().isBlank())
            erro += "Informe um CEP válido e aguarde a localização ser preenchida.\n";
        if (tfNumero.getText().isBlank())
            erro += "Informe o número da casa.\n";
        else if (tfNumero.getText().trim().length() > 10)
            erro += "O número da casa pode ter no máximo 10 caracteres.\n";
        if (cbTipoPagamento.getValue() == null)
            erro += "Selecione o tipo de pagamento.\n";
        if (itens.isEmpty())
            erro += "Adicione pelo menos um produto ao pedido.\n";
        return erro;
    }

    private int lerQuantidade() {
        int quantidade;
        try {
            quantidade = Integer.parseInt(spQuantidade.getEditor().getText().trim());
        } catch (NumberFormatException e) {
            quantidade = 0;
        }
        return quantidade;
    }

    private int posicaoDoProduto(int idProduto) {
        int posicao = -1;
        for (int i = 0; i < itens.size() && posicao == -1; i++)
            if (itens.get(i).produto().getId() == idProduto)
                posicao = i;
        return posicao;
    }

    private double calcularTotal() {
        double total = 0;
        for (int i = 0; i < itens.size(); i++)
            total += itens.get(i).valor();
        return Formato.arredondar(total);
    }

    private void atualizarTotal() {
        lbTotal.setText(Formato.valor(calcularTotal()));
    }

    // Todo pedido novo começa como "Pendente"
    private Status statusPendente() {
        Status status = null;
        List<Status> lista = new StatusDal().get("st_descr ILIKE 'pendente'");
        if (!lista.isEmpty())
            status = lista.get(0);
        return status;
    }


    private void bloquearEdicao() {
        tfNome.setEditable(false);
        tfTelefone.setEditable(false);
        tfCep.setEditable(false);
        tfNumero.setEditable(false);
        dpData.setDisable(true);
        cbTipoPagamento.setDisable(true);
        hbAdicionar.setDisable(true);
        btCancelar.setText("Fechar");
        btImprimir.setDisable(false);
    }
}
