module trioparadadura.example.manedeliveryfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires java.desktop;
    requires org.postgresql.jdbc;
    requires com.github.librepdf.openpdf;

    opens trioparadadura.example.manedeliveryfx to javafx.fxml;
    opens trioparadadura.example.manedeliveryfx.controller to javafx.fxml;
    opens trioparadadura.example.manedeliveryfx.db.entidades to javafx.base;
    exports trioparadadura.example.manedeliveryfx;
}
