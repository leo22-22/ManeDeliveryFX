package trioparadadura.example.manedeliveryfx.db.entidades;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private int id;
    private LocalDate data;
    private String clienteNome;
    private String clienteTelefone;
    private double total;
    private TipoPagamento tipoPagamento;
    private String local;
    private String numeroRes;
    private Status status;
    private List<Item> itens;

    public record Item(Produto produto, int quantidade, double valor) {}


    public Pedido(int id, LocalDate data, String clienteNome, String clienteTelefone, double total, TipoPagamento tipoPagamento, String local, String numeroRes, Status status) {
        this.id = id;
        this.data = data;
        this.clienteNome = clienteNome;
        this.clienteTelefone = clienteTelefone;
        this.total = total;
        this.tipoPagamento = tipoPagamento;
        this.local = local;
        this.numeroRes = numeroRes;
        this.status = status;
        this.itens = itens;
        itens=new ArrayList<>();
    }

    public Pedido(LocalDate data, String clienteNome, String clienteTelefone, double total, TipoPagamento tipoPagamento, String local, String numeroRes, Status status) {
        this(0, data, clienteNome, clienteTelefone, total, tipoPagamento, local, numeroRes, status);
    }

    public Pedido() {
        this(0,LocalDate.now(),"","",0,null,"","",null);
    }

    public void adicionarItem(Produto produto, int quantidade, double valor) {
        this.itens.add(new Item(produto, quantidade, valor));
    }

    // Getters e Setters

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getNumeroRes() {
        return numeroRes;
    }

    public void setNumeroRes(String numeroRes) {
        this.numeroRes = numeroRes;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }
    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public TipoPagamento getTipoPagamento() { return tipoPagamento; }
    public void setTipoPagamento(TipoPagamento tipoPagamento) { this.tipoPagamento = tipoPagamento; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public List<Item> getItens() { return itens; }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", data=" + data +
                ", clienteNome='" + clienteNome + '\'' +
                ", clienteTelefone='" + clienteTelefone + '\'' +
                ", total=" + total +
                ", tipoPagamento=" + tipoPagamento +
                ", status=" + status +
                ", itens=" + itens +
                '}';
    }
}