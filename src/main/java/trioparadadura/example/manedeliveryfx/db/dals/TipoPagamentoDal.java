package trioparadadura.example.manedeliveryfx.db.dals;

import trioparadadura.example.manedeliveryfx.db.entidades.TipoPagamento;
import trioparadadura.example.manedeliveryfx.db.util.IDAL;
import trioparadadura.example.manedeliveryfx.db.util.SingletonDB;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TipoPagamentoDal implements IDAL<TipoPagamento> {

    @Override
    public boolean gravar(TipoPagamento entidade) {
        String sql = "INSERT INTO tipo_pagamento VALUES (default, '$1')";
        sql = sql.replace("$1", entidade.getNome().replace("'", "''"));
        return SingletonDB.getConexao().manipular(sql);
    }

    @Override
    public boolean alterar(TipoPagamento entidade) {
        String sql = "UPDATE tipo_pagamento SET tpg_nome='$1' WHERE tpg_id=$2";
        sql = sql.replace("$1", entidade.getNome().replace("'", "''"));
        sql = sql.replace("$2", String.valueOf(entidade.getId()));
        return SingletonDB.getConexao().manipular(sql);
    }

    @Override
    public boolean apagar(TipoPagamento entidade) {
        return SingletonDB.getConexao().manipular("DELETE FROM tipo_pagamento WHERE tpg_id=" + entidade.getId());
    }

    @Override
    public TipoPagamento get(int id) {
        TipoPagamento tipoPagamento = null;
        String sql = "SELECT * FROM tipo_pagamento WHERE tpg_id=" + id;
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            if (rs.next()) {
                tipoPagamento = new TipoPagamento(rs.getInt("tpg_id"), rs.getString("tpg_nome"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Adicione uma melhor tratativa de exceção se necessário
        }
        return tipoPagamento;
    }

    @Override
    public List<TipoPagamento> get(String filtro) {
        List<TipoPagamento> tipoPagamentoList = new ArrayList<>();
        String sql = "SELECT * FROM tipo_pagamento";
        if (!filtro.isEmpty()) {
            sql += " WHERE " + filtro;
        }
        sql+=" ORDER BY tpg_id";
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            while (rs.next()) {
                TipoPagamento tipoPagamento = new TipoPagamento(rs.getInt("tpg_id"), rs.getString("tpg_nome"));
                tipoPagamentoList.add(tipoPagamento);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Adicione uma melhor tratativa de exceção se necessário
        }
        return tipoPagamentoList;
    }
}