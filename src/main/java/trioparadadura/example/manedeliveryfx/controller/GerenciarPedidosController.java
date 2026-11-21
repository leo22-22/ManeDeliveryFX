package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.ManeDeliveryApplication;
import trioparadadura.example.manedeliveryfx.service.Relatorios;
import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Formato;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.PedidoDal;
import trioparadadura.example.manedeliveryfx.db.dals.StatusDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Pedido;
import trioparadadura.example.manedeliveryfx.db.entidades.Status;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GerenciarPedidosController implements Initializable {

    @FXML
    private DatePicker dpInicio;
    @FXML
    private DatePicker dpFim;
    @FXML
    private ComboBox<Status> cbStatus;
    @FXML
    private TableView<Pedido> tableView;
    @FXML
    private TableColumn<Pedido, String> coId;
    @FXML
    private TableColumn<Pedido, String> coData;
    @FXML
    private TableColumn<Pedido, String> coCliente;
    @FXML
    private TableColumn<Pedido, String> coTelefone;
    @FXML
    private TableColumn<Pedido, String> coPagamento;
    @FXML
    private TableColumn<Pedido, String> coStatus;
    @FXML
    private TableColumn<Pedido, String> coTotal;
    @FXML
    private Label lbResumo;

    private String filtroAtual = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskFieldUtil.dateField(dpInicio.getEditor());
        MaskFieldUtil.dateField(dpFim.getEditor());

        cbStatus.setItems(FXCollections.observableArrayList(new StatusDal().get("")));

        coId.setCellValueFactory(c -> new SimpleStringProperty("" + c.getValue().getId()));
        coData.setCellValueFactory(c -> new SimpleStringProperty(Formato.data(c.getValue().getData())));
        coCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClienteNome()));
        coTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClienteTelefone()));
        coPagamento.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getTipoPagamento())));
        coStatus.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getStatus())));
        coTotal.setCellValueFactory(c -> new SimpleStringProperty(Formato.valor(c.getValue().getTotal())));

        // duplo clique abre os detalhes do pedido
        tableView.setRowFactory(tv -> {
            TableRow<Pedido> linha = new TableRow<>();
            linha.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !linha.isEmpty())
                    abrirDetalhes(linha.getItem());
            });
            return linha;
        });

        carregarTabela();
    }

    @FXML
    void onFiltrar(ActionEvent event) {
        String filtro = "";
        if (dpInicio.getValue() != null)
            filtro = juntar(filtro, "ped_data >= '" + dpInicio.getValue() + "'");
        if (dpFim.getValue() != null)
            filtro = juntar(filtro, "ped_data <= '" + dpFim.getValue() + "'");
        if (cbStatus.getValue() != null)
            filtro = juntar(filtro, "st_id = " + cbStatus.getValue().getId());
        filtroAtual = filtro;
        carregarTabela();
    }

    @FXML
    void onLimpar(ActionEvent event) {
        dpInicio.setValue(null);
        dpFim.setValue(null);
        cbStatus.setValue(null);
        filtroAtual = "";
        carregarTabela();
    }

    @FXML
    void onDetalhes(ActionEvent event) {
        Pedido pedido = selecionado();
        if (pedido != null)
            abrirDetalhes(pedido);
    }

    @FXML
    void onImprimir(ActionEvent event) {
        Pedido pedido = selecionado();
        if (pedido != null)
            Relatorios.pedido(pedido, tableView.getScene().getWindow());
    }

    @FXML
    void onAtualizarStatus(ActionEvent event) {
        Pedido pedido = selecionado();
        if (pedido != null) {
            List<Status> lista = new StatusDal().get("");
            Status atual = null;
            for (int i = 0; i < lista.size(); i++)
                if (pedido.getStatus() != null && lista.get(i).getId() == pedido.getStatus().getId())
                    atual = lista.get(i);

            ChoiceDialog<Status> dialog = new ChoiceDialog<>(atual, lista);
            dialog.setTitle("Atualizar pedido");
            dialog.setHeaderText("Pedido nº " + pedido.getId() + " - " + pedido.getClienteNome());
            dialog.setContentText("Novo status:");
            Optional<Status> escolhido = dialog.showAndWait();
            if (escolhido.isPresent() && escolhido.get() != atual) {
                pedido.setStatus(escolhido.get());
                PedidoDal pedidoDal = new PedidoDal();
                if (pedidoDal.alterar(pedido))
                    carregarTabela();
                else
                    Mensagem.erro(pedidoDal.getErro());
            }
        }
    }

    @FXML
    void onApagar(ActionEvent event) {
        Pedido pedido = selecionado();
        if (pedido != null) {
            if (Mensagem.confirmar("Apagar o pedido nº " + pedido.getId() + " de " + pedido.getClienteNome()
                    + "?\nOs produtos voltam para o estoque.")) {
                PedidoDal pedidoDal = new PedidoDal();
                if (pedidoDal.apagar(pedido))
                    carregarTabela();
                else
                    Mensagem.erro(pedidoDal.getErro());
            }
        }
    }

    private void abrirDetalhes(Pedido pedido) {
        try {
            FXMLLoader loader = new FXMLLoader(ManeDeliveryApplication.class.getResource("view/novo-pedido-view.fxml"));
            Scene scene = new Scene(loader.load());
            NovoPedidoController controller = loader.getController();
            controller.setPedido(pedido);
            Stage stage = new Stage();
            stage.setTitle("Pedido nº " + pedido.getId());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            Mensagem.erro("Erro ao abrir o pedido: " + e.getMessage());
        }
    }

    private Pedido selecionado() {
        Pedido pedido = tableView.getSelectionModel().getSelectedItem();
        if (pedido == null)
            Mensagem.erro("Selecione um pedido na tabela.");
        return pedido;
    }

    private void carregarTabela() {
        List<Pedido> pedidos = new PedidoDal().get(filtroAtual);
        tableView.setItems(FXCollections.observableArrayList(pedidos));
        double total = 0;
        for (int i = 0; i < pedidos.size(); i++)
            total += pedidos.get(i).getTotal();
        lbResumo.setText(pedidos.size() + " pedidos  |  Total: " + Formato.moeda(total));
    }

    private String juntar(String filtro, String condicao) {
        String resultado = condicao;
        if (!filtro.isEmpty())
            resultado = filtro + " AND " + condicao;
        return resultado;
    }

    private String texto(Object o) {
        String t = "-";
        if (o != null)
            t = o.toString();
        return t;
    }
}
