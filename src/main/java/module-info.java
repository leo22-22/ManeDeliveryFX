module trioparadadura.example.manedeliveryfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens trioparadadura.example.manedeliveryfx to javafx.fxml;
    opens trioparadadura.example.manedeliveryfx.db.entidades to javafx.base;
    exports trioparadadura.example.manedeliveryfx;
}
