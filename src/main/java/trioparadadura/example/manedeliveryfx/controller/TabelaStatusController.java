package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.ManeDeliveryApplication;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.StatusDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Status;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TabelaStatusController implements Initializable {

    @FXML
    private TableColumn<Status, Integer> coId;
    @FXML
    private TableColumn<Status, String> coDescricao;
    @FXML
    private TableColumn<Status, String> coObservacao;
    @FXML
    private TableColumn<Status, String> coSucesso;
    @FXML
    private TableView<Status> tableView;

    @FXML
    void onNovoStatus(ActionEvent event) throws Exception {
        abrirFormulario(null);
    }

    @FXML
    void onAlterarStatus(ActionEvent event) throws Exception {
        Status status = tableView.getSelectionModel().getSelectedItem();
        if (status != null)
            abrirFormulario(status);
    }

    @FXML
    void onApagarStatus(ActionEvent event) {
        Status status = tableView.getSelectionModel().getSelectedItem();
        if (status != null) {
            if (Mensagem.confirmar("Apagar o status " + status.getDescricao() + "?")) {
                if (new StatusDal().apagar(status))
                    carregarTabela();
                else
                    Mensagem.erro("Existem pedidos com este status.");
            }
        }
    }

    private void abrirFormulario(Status status) throws Exception {
        FXMLLoader loader = new FXMLLoader(ManeDeliveryApplication.class.getResource("view/status-form-view.fxml"));
        Scene scene = new Scene(loader.load());
        if (status != null) {
            StatusFormController controller = loader.getController();
            controller.setStatus(status);
        }
        Stage stage = new Stage();
        stage.setTitle(status == null ? "Novo Status" : "Alterar Status");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.showAndWait();
        carregarTabela();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        coId.setCellValueFactory(new PropertyValueFactory<>("id"));
        coDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        coObservacao.setCellValueFactory(new PropertyValueFactory<>("observacao"));
        coSucesso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isSucesso() ? "Sim" : "Não"));
        carregarTabela();
    }

    private void carregarTabela() {
        List<Status> lista = new StatusDal().get("");
        tableView.setItems(FXCollections.observableArrayList(lista));
    }
}
