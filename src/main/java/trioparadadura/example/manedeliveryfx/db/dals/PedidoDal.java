package trioparadadura.example.manedeliveryfx.db.dals;

import trioparadadura.example.manedeliveryfx.db.entidades.Pedido;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;
import trioparadadura.example.manedeliveryfx.db.util.IDAL;
import trioparadadura.example.manedeliveryfx.db.util.SingletonDB;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoDal implements IDAL<Pedido> {

    @Override
    public boolean gravar(Pedido entidade) {
        String sql = "INSERT INTO pedido VALUES (default, '$1', '$2', '$3', $4, $5, '$6', '$7', $8)";
        sql = sql.replace("$1", ""+entidade.getData());
        sql = sql.replace("$2", entidade.getClienteNome());
        sql = sql.replace("$3", entidade.getClienteTelefone());
        sql = sql.replace("$4", String.valueOf(entidade.getTotal()));
        sql = sql.replace("$5", (entidade.getTipoPagamento() != null) ? String.valueOf(entidade.getTipoPagamento().getId()) : "NULL");
        sql = sql.replace("$6", entidade.getLocal()); // Ajuste se números de reservas ou outra informação forem necessárias
        sql = sql.replace("$7", entidade.getNumeroRes()); // Adjust if needed
        sql = sql.replace("$8", entidade.getStatus() != null ? String.valueOf(entidade.getStatus().getId()) : "NULL");

        int id_ped= SingletonDB.getConexao().inserir(sql);

        // Insere os itens do pedido
        for (Pedido.Item item : entidade.getItens()) {
            String itemSql = String.format(Locale.US,"INSERT INTO item (pro_id, ped_id, it_quant, it_valor) VALUES (%d, %d, %d, %.2f)",
                    item.produto().getId(), id_ped, item.quantidade(), item.valor());
            SingletonDB.getConexao().manipular(itemSql);
        }

        return (id_ped!=-1)?true:false;
    }

    @Override
    public boolean alterar(Pedido entidade) {
        String sql = "UPDATE pedido SET ped_data='$1', ped_clinome='$2', ped_clifone='$3', ped_total=$4, tpg_id=$5, st_id=$6 WHERE ped_id=$7";
        sql = sql.replace("$1", entidade.getData().toString());
        sql = sql.replace("$2", entidade.getClienteNome());
        sql = sql.replace("$3", entidade.getClienteTelefone());
        sql = sql.replace("$4", String.valueOf(entidade.getTotal()));
        sql = sql.replace("$5", (entidade.getTipoPagamento() != null) ? String.valueOf(entidade.getTipoPagamento().getId()) : "NULL");
        sql = sql.replace("$6", entidade.getStatus() != null ? String.valueOf(entidade.getStatus().getId()) : "NULL");
        sql = sql.replace("$7", String.valueOf(entidade.getId()));

        // Remover os itens antigos e inserir novos
        String deleteSql = "DELETE FROM item WHERE ped_id=" + entidade.getId();
        SingletonDB.getConexao().manipular(deleteSql);

        boolean result = SingletonDB.getConexao().manipular(sql);

        for (Pedido.Item item : entidade.getItens()) {
            String itemSql = String.format(Locale.US,"INSERT INTO item (pro_id, ped_id, it_quant, it_valor) VALUES (%d, %d, %d, %.2f)",
                    item.produto().getId(), entidade.getId(), item.quantidade(), item.valor());
            SingletonDB.getConexao().manipular(itemSql);
        }

        return result;
    }

    @Override
    public boolean apagar(Pedido entidade) {
        // antes, remover itens associados a este pedido
        SingletonDB.getConexao().manipular("DELETE FROM item WHERE ped_id=" + entidade.getId());

        boolean result = SingletonDB.getConexao().manipular("DELETE FROM pedido WHERE ped_id=" + entidade.getId());
        return result;
    }

    @Override
    public Pedido get(int id) {
        Pedido pedido = null;
        String sql = "SELECT * FROM pedido WHERE ped_id=" + id;
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            if (rs.next()) {
                pedido = new Pedido(
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

                // Recuperar os itens associados a este pedido
                List<Pedido.Item> itens = new ArrayList<>();
                String itemSql = "SELECT * FROM item WHERE ped_id=" + id;
                ResultSet rsItens = SingletonDB.getConexao().consultar(itemSql);
                while (rsItens.next()) {
                    Produto produto = new ProdutoDal().get(rsItens.getInt("pro_id"));
                    itens.add(new Pedido.Item(produto, rsItens.getInt("it_quant"), rsItens.getDouble("it_valor")));
                }
                pedido.getItens().addAll(itens);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Melhorar tratamento
        }
        return pedido;
    }

    @Override
    public List<Pedido> get(String filtro) {
        List<Pedido> pedidoList = new ArrayList<>();
        String sql = "SELECT * FROM pedido";
        if (!filtro.isEmpty()) {
            sql += " WHERE " + filtro;
        }
        sql+=" order by ped_id";
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

                // Recuperar os itens associados a este pedido
                List<Pedido.Item> itens = new ArrayList<>();
                String itemSql = "SELECT * FROM item WHERE ped_id=" + pedido.getId();
                ResultSet rsItens = SingletonDB.getConexao().consultar(itemSql);
                while (rsItens.next()) {
                    Produto produto = new ProdutoDal().get(rsItens.getInt("pro_id"));
                    itens.add(new Pedido.Item(produto, rsItens.getInt("it_quant"), rsItens.getDouble("it_valor")));
                }
                pedido.getItens().addAll(itens);

                pedidoList.add(pedido);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Melhorar tratamento
        }
        return pedidoList;
    }
}