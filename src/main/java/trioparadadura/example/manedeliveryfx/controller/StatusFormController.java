package trioparadadura.example.manedeliveryfx.controller;

import trioparadadura.example.manedeliveryfx.util.MaskFieldUtil;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import trioparadadura.example.manedeliveryfx.db.dals.StatusDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Status;

import java.net.URL;
import java.util.ResourceBundle;

public class StatusFormController implements Initializable {

    @FXML
    private TextField tfDescricao;
    @FXML
    private TextField tfObservacao;
    @FXML
    private CheckBox ckSucesso;

    private Status status = null; // null = novo status

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskFieldUtil.maxField(tfDescricao, 25);
        MaskFieldUtil.maxField(tfObservacao, 80);
    }

    // chamado pela tabela quando for alterar
    public void setStatus(Status status) {
        this.status = status;
        tfDescricao.setText(status.getDescricao());
        tfObservacao.setText(status.getObservacao());
        ckSucesso.setSelected(status.isSucesso());
    }

    @FXML
    void onSalvar(ActionEvent event) {
        String erro = validar();
        if (!erro.isEmpty())
            Mensagem.erro(erro);
        else {
            String descricao = tfDescricao.getText().trim();
            String observacao = tfObservacao.getText().trim();
            boolean sucesso = ckSucesso.isSelected();

            boolean ok;
            if (status == null)
                ok = new StatusDal().gravar(new Status(descricao, observacao, sucesso));
            else {
                status.setDescricao(descricao);
                status.setObservacao(observacao);
                status.setSucesso(sucesso);
                ok = new StatusDal().alterar(status);
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

    private String validar() {
        String erro = "";
        if (tfDescricao.getText().isBlank())
            erro += "Informe a descrição.\n";
        return erro;
    }

    private void fechar() {
        ((Stage) tfDescricao.getScene().getWindow()).close();
    }
}
