package trioparadadura.example.manedeliveryfx.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Consulta o serviço web do ViaCEP (https://viacep.com.br)
public class ViaCep {

    private String logradouro = "";
    private String bairro = "";
    private String cidade = "";
    private String uf = "";
    private String erro = "";

    // Recebe o CEP (com ou sem traço) e retorna true se encontrou o endereço
    public boolean buscar(String cep) {
        boolean encontrou = false;
        String numeros = cep.replaceAll("\\D", "");
        if (numeros.length() != 8)
            erro = "O CEP deve ter 8 números.";
        else {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://viacep.com.br/ws/" + numeros + "/json/"))
                        .timeout(Duration.ofSeconds(8))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String json = response.body();
                if (response.statusCode() != 200 || json.contains("\"erro\""))
                    erro = "CEP não encontrado.";
                else {
                    logradouro = campo(json, "logradouro");
                    bairro = campo(json, "bairro");
                    cidade = campo(json, "localidade");
                    uf = campo(json, "uf");
                    encontrou = true;
                }
            } catch (Exception e) {
                erro = "Não foi possível consultar o ViaCEP. Verifique a internet.";
            }
        }
        return encontrou;
    }

    // Lê o valor de um campo do JSON, ex.: "bairro": "Vila Maria"
    private String campo(String json, String nome) {
        String valor = "";
        Matcher m = Pattern.compile("\"" + nome + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (m.find())
            valor = m.group(1);
        return valor;
    }

    // Monta no mesmo formato dos pedidos que já estão no banco:
    // "Rua Seis, Vila Maria, Presidente Prudente/SP"
    public String getLocalizacao() {
        String local = "";
        if (!logradouro.isEmpty())
            local += logradouro + ", ";
        if (!bairro.isEmpty())
            local += bairro + ", ";
        local += cidade + "/" + uf;
        return local;
    }

    public String getErro() {
        return erro;
    }
}
