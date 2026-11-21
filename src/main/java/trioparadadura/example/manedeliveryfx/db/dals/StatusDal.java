package trioparadadura.example.manedeliveryfx.db.dals;

import trioparadadura.example.manedeliveryfx.db.entidades.Status;
import trioparadadura.example.manedeliveryfx.db.util.IDAL;
import trioparadadura.example.manedeliveryfx.db.util.SingletonDB;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StatusDal implements IDAL<Status> {

    @Override
    public boolean gravar(Status entidade) {
        String sql = "INSERT INTO status VALUES (default, '$1', '$2', $3)";
        sql = sql.replace("$1", entidade.getDescricao().replace("'", "''"));
        sql = sql.replace("$2", entidade.getObservacao().replace("'", "''"));
        sql = sql.replace("$3", entidade.isSucesso() ? "true" : "false");
        return SingletonDB.getConexao().manipular(sql);
    }

    @Override
    public boolean alterar(Status entidade) {
        String sql = "UPDATE status SET st_descr='$1', st_obs='$2', st_sucesso=$3 WHERE st_id=$4";
        sql = sql.replace("$1", entidade.getDescricao().replace("'", "''"));
        sql = sql.replace("$2", entidade.getObservacao().replace("'", "''"));
        sql = sql.replace("$3", entidade.isSucesso() ? "true" : "false");
        sql = sql.replace("$4", String.valueOf(entidade.getId()));
        return SingletonDB.getConexao().manipular(sql);
    }

    @Override
    public boolean apagar(Status entidade) {
        return SingletonDB.getConexao().manipular("DELETE FROM status WHERE st_id=" + entidade.getId());
    }

    @Override
    public Status get(int id) {
        Status status = null;
        String sql = "SELECT * FROM status WHERE st_id=" + id;
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            if (rs.next()) {
                status = new Status(
                        rs.getInt("st_id"),
                        rs.getString("st_descr"),
                        rs.getString("st_obs"),
                        rs.getBoolean("st_sucesso")
                );
            }
        } catch (Exception e) {
            e.printStackTrace(); // Melhor tratamento de exceção pode ser implementado
        }
        return status;
    }

    @Override
    public List<Status> get(String filtro) {
        List<Status> statusList = new ArrayList<>();
        String sql = "SELECT * FROM status";
        if (!filtro.isEmpty()) {
            sql += " WHERE " + filtro;
        }
        sql+=" ORDER BY st_id";
        ResultSet rs = SingletonDB.getConexao().consultar(sql);
        try {
            while (rs.next()) {
                Status status = new Status(
                        rs.getInt("st_id"),
                        rs.getString("st_descr"),
                        rs.getString("st_obs"),
                        rs.getBoolean("st_sucesso")
                );
                statusList.add(status);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Melhor tratamento de exceção pode ser implementado
        }
        return statusList;
    }
}