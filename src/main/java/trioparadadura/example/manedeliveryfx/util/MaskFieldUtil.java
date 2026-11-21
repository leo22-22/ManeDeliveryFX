package trioparadadura.example.manedeliveryfx.util;

/*
Autor: Rogério M. de Queiroz
modificado por Silvio A. Carro
modificado por Bruno A. da S. Melo e Gabriel B. Salvador

Ajuste no ManeDeliveryFX: os métodos e o modo de usar continuam os mesmos,
mas a formatação agora é feita com TextFormatter. A versão anterior trocava
o texto com Platform.runLater dentro de listeners, o que embaralhava os
caracteres ao digitar rápido e gerava "The start must be <= the end" ao
apagar com Backspace.
*/

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public abstract class MaskFieldUtil {

    private static final String FORMATO = "mask.formato";
    private static final String TAMANHO = "mask.tamanho";

    private static List<KeyCode> ignoreKeyCodes = new ArrayList<>();

    public static void ignoreKeys(TextField textField) {
        textField.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (ignoreKeyCodes.contains(keyEvent.getCode())) {
                keyEvent.consume();
            }
        });
    }

    //xxxxx-xxxxx-xxxxx-xxxxx
    public static void serialTextField(final TextField textField) {
        aplicar(textField, 23, value -> {
            value = value.replaceAll("[^\\w]", "");
            value = value.replaceFirst("(\\w{5})(\\w)", "$1-$2");
            value = value.replaceFirst("(\\w{5})\\-(\\w{5})(\\w)", "$1-$2-$3");
            value = value.replaceFirst("(\\w{5})\\-(\\w{5})\\-(\\w{5})(\\w)", "$1-$2-$3-$4");
            return value.toUpperCase();
        });
    }

    // dd/mm/aaaa
    public static void dateField(final TextField textField) {
        aplicar(textField, 10, value -> {
            value = value.replaceAll("[^0-9]", "");
            value = value.replaceFirst("(\\d{2})(\\d)", "$1/$2");
            value = value.replaceFirst("(\\d{2})\\/(\\d{2})(\\d)", "$1/$2/$3");
            return value;
        });
    }

    public static void numericField(final TextField textField) {
        aplicar(textField, null, value -> value.replaceAll("[^0-9]", ""));
    }

    // 1.234,56
    public static void monetaryField(final TextField textField) {
        textField.setAlignment(Pos.CENTER_RIGHT);
        aplicar(textField, 17, value -> {
            value = value.replaceAll("[^0-9]", "");
            value = value.replaceAll("([0-9]{1})([0-9]{14})$", "$1.$2");
            value = value.replaceAll("([0-9]{1})([0-9]{11})$", "$1.$2");
            value = value.replaceAll("([0-9]{1})([0-9]{8})$", "$1.$2");
            value = value.replaceAll("([0-9]{1})([0-9]{5})$", "$1.$2");
            value = value.replaceAll("([0-9]{1})([0-9]{2})$", "$1,$2");
            return value;
        });
        // ao sair do campo, "5" vira "5,00"
        textField.focusedProperty().addListener((observableValue, aBoolean, fieldChange) -> {
            int length;
            if (!(fieldChange || (length = textField.getText().length()) <= 0 || length >= 3)) {
                textField.setText(textField.getText() + "00");
            }
        });
    }

    public static BigDecimal monetaryValueFromField(TextField textField) {
        if (textField.getText().isEmpty()) {
            return null;
        }
        BigDecimal retorno = BigDecimal.ZERO;
        NumberFormat nf = NumberFormat.getNumberInstance();
        try {
            Number parsedNumber = nf.parse(textField.getText());
            retorno = new BigDecimal(parsedNumber.toString());
        } catch (ParseException ex) {
            Logger.getLogger(MaskFieldUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }

    // até 11 números vira CPF, acima disso CNPJ
    public static void cpfCnpjField(TextField textField) {
        aplicar(textField, 18, value -> {
            value = value.replaceAll("[^0-9]", "");
            if (value.length() <= 11) {
                value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
                value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
                value = value.replaceFirst("(\\d{3})(\\d)", "$1-$2");
            } else {
                value = value.replaceFirst("(\\d{2})(\\d)", "$1.$2");
                value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
                value = value.replaceFirst("(\\d{3})(\\d)", "$1/$2");
                value = value.replaceFirst("(\\d{4})(\\d)", "$1-$2");
            }
            return value;
        });
    }

    // 00000-000
    public static void cepField(TextField textField) {
        aplicar(textField, 9, value -> {
            value = value.replaceAll("[^0-9]", "");
            value = value.replaceFirst("(\\d{5})(\\d)", "$1-$2");
            return value;
        });
    }

    // (00) 0000-0000 ou (00) 00000-0000
    public static void foneField(TextField textField) {
        aplicar(textField, 15, value -> {
            value = value.replaceAll("[^0-9]", "");
            int tam = value.length();
            value = value.replaceFirst("(\\d{2})(\\d)", "($1) $2");
            value = value.replaceFirst("(\\d{4})(\\d)", "$1-$2");
            if (tam > 10) {
                value = value.replaceAll("-", "");
                value = value.replaceFirst("(\\d{5})(\\d)", "$1-$2");
            }
            return value;
        });
    }

    // 000.000.000-00
    public static void cpfField(TextField textField) {
        aplicar(textField, 14, value -> {
            value = value.replaceAll("[^0-9]", "");
            value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
            value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
            value = value.replaceFirst("(\\d{3})(\\d)", "$1-$2");
            return value;
        });
    }

    // 00.000.000/0000-00
    public static void cnpjField(TextField textField) {
        aplicar(textField, 18, value -> {
            value = value.replaceAll("[^0-9]", "");
            value = value.replaceFirst("(\\d{2})(\\d)", "$1.$2");
            value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
            value = value.replaceFirst("(\\d{3})(\\d)", "$1/$2");
            value = value.replaceFirst("(\\d{4})(\\d)", "$1-$2");
            return value;
        });
    }

    public static void maxField(TextField textField, Integer length) {
        aplicar(textField, length, null);
    }

    public static String onlyDigitsValue(TextField field) {
        String result = field.getText();
        if (result == null) {
            return null;
        }
        return result.replaceAll("[^0-9]", "");
    }

    public static String onlyAlfaNumericValue(TextField field) {
        String result = field.getText();
        if (result == null) {
            return null;
        }
        return result.replaceAll("[^0-9a-zA-Z]", "");
    }

    // Guarda o formato e o tamanho máximo no próprio campo e instala um único
    // TextFormatter que usa os dois. Assim dá para combinar, por exemplo,
    // numericField + maxField no mesmo TextField.
    @SuppressWarnings("unchecked")
    private static void aplicar(TextField textField, Integer tamanho, UnaryOperator<String> formato) {
        if (formato != null) {
            UnaryOperator<String> anterior = (UnaryOperator<String>) textField.getProperties().get(FORMATO);
            if (anterior == null) {
                textField.getProperties().put(FORMATO, formato);
            } else {
                UnaryOperator<String> combinado = value -> formato.apply(anterior.apply(value));
                textField.getProperties().put(FORMATO, combinado);
            }
        }
        if (tamanho != null) {
            textField.getProperties().put(TAMANHO, tamanho);
        }
        if (textField.getTextFormatter() == null) {
            textField.setTextFormatter(new TextFormatter<String>(change -> formatar(textField, change)));
        }
    }

    @SuppressWarnings("unchecked")
    private static TextFormatter.Change formatar(TextField textField, TextFormatter.Change change) {
        if (change.isContentChange()) {
            String novo = change.getControlNewText();
            UnaryOperator<String> formato = (UnaryOperator<String>) textField.getProperties().get(FORMATO);
            if (formato != null) {
                novo = formato.apply(novo);
            }
            Integer tamanho = (Integer) textField.getProperties().get(TAMANHO);
            if (tamanho != null && novo.length() > tamanho) {
                novo = change.getControlText(); // passou do limite: mantém o texto anterior
            }
            change.setRange(0, change.getControlText().length());
            change.setText(novo);
            change.setCaretPosition(novo.length());
            change.setAnchor(novo.length());
        }
        return change;
    }

    static {
        Collections.addAll(ignoreKeyCodes, new KeyCode[]{KeyCode.F1, KeyCode.F2, KeyCode.F3, KeyCode.F4, KeyCode.F5, KeyCode.F6, KeyCode.F7, KeyCode.F8, KeyCode.F9, KeyCode.F10, KeyCode.F11, KeyCode.F12});
    }
}
