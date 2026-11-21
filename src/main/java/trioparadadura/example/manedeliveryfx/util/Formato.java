package trioparadadura.example.manedeliveryfx.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Formato {

    private static final Locale BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String valor(double v) {
        return String.format(BR, "%,.2f", v);
    }

    public static String moeda(double v) {
        return "R$ " + valor(v);
    }

    public static String data(LocalDate d) {
        String texto = "";
        if (d != null)
            texto = d.format(DATA);
        return texto;
    }

    public static double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
