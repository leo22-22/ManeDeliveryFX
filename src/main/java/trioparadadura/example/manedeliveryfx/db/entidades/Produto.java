package trioparadadura.example.manedeliveryfx.db.entidades;

import java.time.LocalDate;

public class Produto {
    private int id;
    private String nome;
    private double preco;
    private int estoque;
    private Categoria categoria;
    private Marca marca;

    public Produto(int id, String nome, double preco, int estoque, Categoria categoria, Marca marca) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.marca = marca;
    }

    public Produto(String nome, double preco, int estoque, Categoria categoria, Marca marca) {
        this(0,nome,preco,estoque,categoria,marca);
    }

    public Produto() {
        this(0,"",0,0,null,null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Marca getMarca() {
        return marca;
    }

    public void setMarca(Marca marca) {
        this.marca = marca;
    }
}
