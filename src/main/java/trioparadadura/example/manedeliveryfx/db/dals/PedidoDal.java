package trioparadadura.example.manedeliveryfx.db.dals;

import trioparadadura.example.manedeliveryfx.db.entidades.Pedido;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;
import trioparadadura.example.manedeliveryfx.db.util.IDAL;
import trioparadadura.example.manedeliveryfx.db.util.SingletonDB;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

// O pedido usa PreparedStatement e transação: o pedido, os itens e a baixa
// no estoque são gravados juntos, ou nada é gravado.
public class PedidoDal implements IDAL<Pedido> {

    private String erro = "";

    public String getErro() {
        return erro;
    }

    @Override
    public boolean gravar(Pedido entidade) {
        boolean ok = false;
        erro = "";
        Connection con = SingletonDB.getConexao().getConnect();
        try {
            con.setAutoCommit(false);

            String sql = """
                    INSERT INTO pedido (ped_data, ped_clinome, ped_clifone, ped_total, tpg_id, ped_local, ped_numero_res, st_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preencherCabecalho(ps, entidade);
            ps.executeUpdate();
            ResultSet chave = ps.getGeneratedKeys();
            if (chave.next())
                entidade.setId(chave.getInt(1));
            ps.close();

            PreparedStatement psItem = con.prepareStatement(
                    "INSERT INTO item (pro_id, ped_id, it_quant, it_valor) VALUES (?, ?, ?, ?)");
            PreparedStatement psEstoque = con.prepareStatement(
                    "UPDATE produto SET pro_estoque = pro_estoque - ? WHERE pro_id = ? AND pro_estoque >= ?");
            List<Pedido.Item> itens = entidade.getItens();
            boolean estoqueOk = true;
            for (int i = 0; i < itens.size() && estoqueOk; i++) {
                Pedido.Item item = itens.get(i);
                psItem.setInt(1, item.produto().getId());
                psItem.setInt(2, entidade.getId());
                psItem.setInt(3, item.quantidade());
                psItem.setDouble(4, item.valor());
                psItem.executeUpdate();

                psEstoque.setInt(1, item.quantidade());
                psEstoque.setInt(2, item.produto().getId());
                psEstoque.setInt(3, item.quantidade());
                if (psEstoque.executeUpdate() == 0) {
                    estoqueOk = false;
                    erro = "Estoque insuficiente para " + item.produto().getNome() + ".";
                }
            }
            psItem.close();
            psEstoque.close();

            if (estoqueOk) {
                con.commit();
                ok = true;
            } else {
                con.rollback();
                entidade.setId(0);
            }
        } catch (SQLException e) {
            erro = "Erro ao gravar o pedido: " + e.getMessage();
            desfazer(con);
            entidade.setId(0);
        } finally {
            voltarAutoCommit(con);
        }
        return ok;
    }

    // Altera só os dados do pedido (cliente, pagamento, status...).
    // Os itens não mudam depois de gravados, para não bagunçar o estoque.
    @Override
    public boolean alterar(Pedido entidade) {
        boolean ok = false;
        erro = "";
        String sql = """
                UPDATE pedido SET ped_data=?, ped_clinome=?, ped_clifone=?, ped_total=?, tpg_id=?,
                ped_local=?, ped_numero_res=?, st_id=?
                WHERE ped_id=?""";
        try {
            PreparedStatement ps = SingletonDB.getConexao().getConnect().prepareStatement(sql);
            preencherCabecalho(ps, entidade);
            ps.setInt(9, entidade.getId());
            ok = ps.executeUpdate() > 0;
            ps.close();
        } catch (SQLException e) {
            erro = "Erro ao alterar o pedido: " + e.getMessage();
        }
        return ok;
    }

    // Apaga o pedido e devolve os produtos ao estoque
    @Override
    public boolean apagar(Pedido entidade) {
        boolean ok = false;
        erro = "";
        Connection con = SingletonDB.getConexao().getConnect();
        try {
            con.setAutoCommit(false);

            PreparedStatement psEstoque = con.prepareStatement("""
                    UPDATE produto SET pro_estoque = pro_estoque + i.it_quant
                    FROM item i
                    WHERE i.pro_id = produto.pro_id AND i.ped_id = ?""");
            psEstoque.setInt(1, entidade.getId());
            psEstoque.executeUpdate();
            psEstoque.close();

            PreparedStatement psItens = con.prepareStatement("DELETE FROM item WHERE ped_id = ?");
            psItens.setInt(1, entidade.getId());
            psItens.executeUpdate();
            psItens.close();

            PreparedStatement psPedido = con.prepareStatement("DELETE FROM pedido WHERE ped_id = ?");
            psPedido.setInt(1, entidade.getId());
            ok = psPedido.executeUpdate() > 0;
            psPedido.close();

            if (ok)
                con.commit();
            else
                con.rollback();
        } catch (SQLException e) {
            erro = "Erro ao apagar o pedido: " + e.getMessage();
            desfazer(con);
        } finally {
            voltarAutoCommit(con);
        }
        return ok;
    }

    @Override
    public Pedido get(int id) {
        Pedido pedido = null;
        List<Pedido> lista = get("ped_id=" + id);
        if (!lista.isEmpty())
            pedido = lista.get(0);
        return pedido;
    }

    @Override
    public List<Pedido> get(String filtro) {
        List<Pedido> pedidoList = new ArrayList<>();
        String sql = "SELECT * FROM pedido";
        if (!filtro.isEmpty())
            sql += " WHERE " + filtro;
        sql += " ORDER BY ped_id";
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            while (rs.next()) {
                Pedido pedido = new Pedido(
                        rs.getInt("ped_id"),
                        rs.getDate("ped_data").toLocalDate(),
                        rs.getString("ped_clinome"),
                        rs.getString("ped_clifone"),
                        rs.getDouble("ped_total"),
                        new TipoPagamentoDal().get(rs.getInt("tpg_id")),
                        rs.getString("ped_local"),
                        rs.getString("ped_numero_res"),
                        new StatusDal().get(rs.getInt("st_id"))
                );
                carregarItens(pedido);
                pedidoList.add(pedido);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pedidoList;
    }

    private void carregarItens(Pedido pedido) throws SQLException {
        ResultSet rs = SingletonDB.getConexao().consultar("SELECT * FROM item WHERE ped_id=" + pedido.getId());
        ProdutoDal produtoDal = new ProdutoDal();
        while (rs.next()) {
            Produto produto = produtoDal.get(rs.getInt("pro_id"));
            pedido.adicionarItem(produto, rs.getInt("it_quant"), rs.getDouble("it_valor"));
        }
    }

    private void preencherCabecalho(PreparedStatement ps, Pedido p) throws SQLException {
        ps.setDate(1, Date.valueOf(p.getData()));
        ps.setString(2, p.getClienteNome());
        ps.setString(3, p.getClienteTelefone());
        ps.setDouble(4, p.getTotal());
        if (p.getTipoPagamento() != null)
            ps.setInt(5, p.getTipoPagamento().getId());
        else
            ps.setNull(5, Types.INTEGER);
        ps.setString(6, p.getLocal());
        ps.setString(7, p.getNumeroRes());
        if (p.getStatus() != null)
            ps.setInt(8, p.getStatus().getId());
        else
            ps.setNull(8, Types.INTEGER);
    }

    private void desfazer(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void voltarAutoCommit(Connection con) {
        try {
            con.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
