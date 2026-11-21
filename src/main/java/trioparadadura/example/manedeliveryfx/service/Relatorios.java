package trioparadadura.example.manedeliveryfx.service;

import trioparadadura.example.manedeliveryfx.util.Formato;
import trioparadadura.example.manedeliveryfx.util.Mensagem;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import trioparadadura.example.manedeliveryfx.db.dals.PedidoDal;
import trioparadadura.example.manedeliveryfx.db.dals.ProdutoDal;
import trioparadadura.example.manedeliveryfx.db.entidades.Pedido;
import trioparadadura.example.manedeliveryfx.db.entidades.Produto;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

// Relatórios em PDF (biblioteca OpenPDF)
public class Relatorios {

    private static final Color CINZA = new Color(225, 225, 225);
    private static final Color ROXO = new Color(91, 31, 209);

    private static final Font TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, ROXO);
    private static final Font SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font PEQUENO = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

    // ------------------------------------------------------------ Pedido

    public static void pedido(Pedido pedido, Window janela) {
        File arquivo = escolherArquivo(janela, "pedido-" + pedido.getId() + ".pdf");
        if (arquivo != null) {
            try {
                Document doc = abrirDocumento(arquivo);
                cabecalho(doc, "Pedido nº " + pedido.getId());

                PdfPTable dados = new PdfPTable(new float[]{1.3f, 5f});
                dados.setWidthPercentage(100);
                linhaDado(dados, "Data", Formato.data(pedido.getData()));
                linhaDado(dados, "Cliente", pedido.getClienteNome());
                linhaDado(dados, "Telefone", pedido.getClienteTelefone());
                linhaDado(dados, "Endereço", pedido.getLocal() + "  |  Nº " + pedido.getNumeroRes());
                linhaDado(dados, "Pagamento", texto(pedido.getTipoPagamento()));
                linhaDado(dados, "Status", texto(pedido.getStatus()));
                doc.add(dados);

                Paragraph p = new Paragraph("Itens do pedido", SUBTITULO);
                p.setSpacingBefore(14);
                p.setSpacingAfter(6);
                doc.add(p);

                PdfPTable tabela = new PdfPTable(new float[]{5f, 1.2f, 1.6f, 1.6f});
                tabela.setWidthPercentage(100);
                titulos(tabela, "Produto", "Qtd", "Preço R$", "Total R$");
                List<Pedido.Item> itens = pedido.getItens();
                for (int i = 0; i < itens.size(); i++) {
                    Pedido.Item item = itens.get(i);
                    celula(tabela, item.produto().getNome() + " - " + item.produto().getMarca(), Element.ALIGN_LEFT);
                    celula(tabela, "" + item.quantidade(), Element.ALIGN_CENTER);
                    celula(tabela, Formato.valor(item.produto().getPreco()), Element.ALIGN_RIGHT);
                    celula(tabela, Formato.valor(item.valor()), Element.ALIGN_RIGHT);
                }
                linhaTotal(tabela, "TOTAL DO PEDIDO", Formato.moeda(pedido.getTotal()), 4);
                doc.add(tabela);

                doc.close();
                abrir(arquivo);
            } catch (Exception e) {
                Mensagem.erro("Erro ao gerar o PDF: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ Lista de preços

    public static void listaPrecos(Window janela) {
        File arquivo = escolherArquivo(janela, "lista-de-precos.pdf");
        if (arquivo != null) {
            try {
                List<Produto> produtos = new ProdutoDal().get("");
                produtos.sort(Comparator
                        .comparing((Produto pr) -> pr.getCategoria().getNome().toLowerCase())
                        .thenComparing(pr -> pr.getNome().toLowerCase()));

                Document doc = abrirDocumento(arquivo);
                cabecalho(doc, "Lista de Preços");

                PdfPTable tabela = new PdfPTable(new float[]{5f, 2.5f, 1.4f, 1.6f});
                tabela.setWidthPercentage(100);
                titulos(tabela, "Produto", "Marca", "Volume", "Preço");

                String categoriaAtual = "";
                for (int i = 0; i < produtos.size(); i++) {
                    Produto pr = produtos.get(i);
                    String categoria = pr.getCategoria().getNome();
                    if (!categoria.equalsIgnoreCase(categoriaAtual)) {
                        categoriaAtual = categoria;
                        PdfPCell grupo = new PdfPCell(new Phrase(categoria.toUpperCase(), NEGRITO));
                        grupo.setColspan(4);
                        grupo.setBackgroundColor(CINZA);
                        grupo.setPadding(5);
                        tabela.addCell(grupo);
                    }
                    celula(tabela, pr.getNome(), Element.ALIGN_LEFT);
                    celula(tabela, texto(pr.getMarca()), Element.ALIGN_LEFT);
                    celula(tabela, pr.getVolume() + " ml", Element.ALIGN_CENTER);
                    celula(tabela, Formato.moeda(pr.getPreco()), Element.ALIGN_RIGHT);
                }
                doc.add(tabela);

                Paragraph rodape = new Paragraph(produtos.size() + " produtos cadastrados. Preços sujeitos a alteração.", PEQUENO);
                rodape.setSpacingBefore(8);
                doc.add(rodape);

                doc.close();
                abrir(arquivo);
            } catch (Exception e) {
                Mensagem.erro("Erro ao gerar o PDF: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------ Pedidos por data

    public static void pedidosPorData(LocalDate inicio, LocalDate fim, Window janela) {
        List<Pedido> pedidos = new PedidoDal().get("ped_data BETWEEN '" + inicio + "' AND '" + fim + "'");
        if (pedidos.isEmpty())
            Mensagem.info("Nenhum pedido encontrado entre " + Formato.data(inicio) + " e " + Formato.data(fim) + ".");
        else {
            File arquivo = escolherArquivo(janela, "pedidos-" + inicio + "-a-" + fim + ".pdf");
            if (arquivo != null) {
                try {
                    pedidos.sort(Comparator.comparing(Pedido::getData).thenComparing(Pedido::getId));

                    Document doc = abrirDocumento(arquivo);
                    cabecalho(doc, "Pedidos por Data");
                    Paragraph periodo = new Paragraph("Período: " + Formato.data(inicio) + " a " + Formato.data(fim), NORMAL);
                    periodo.setSpacingAfter(8);
                    doc.add(periodo);

                    PdfPTable tabela = new PdfPTable(new float[]{0.8f, 3.6f, 2.4f, 2f, 2.4f, 1.7f});
                    tabela.setWidthPercentage(100);
                    titulos(tabela, "Nº", "Cliente", "Telefone", "Pagamento", "Status", "Total R$");

                    LocalDate dataAtual = null;
                    double subtotal = 0;
                    double totalGeral = 0;
                    for (int i = 0; i < pedidos.size(); i++) {
                        Pedido p = pedidos.get(i);
                        if (!p.getData().equals(dataAtual)) {
                            if (dataAtual != null)
                                linhaTotal(tabela, "Subtotal do dia", Formato.valor(subtotal), 6);
                            dataAtual = p.getData();
                            subtotal = 0;
                            PdfPCell grupo = new PdfPCell(new Phrase(Formato.data(dataAtual), NEGRITO));
                            grupo.setColspan(6);
                            grupo.setBackgroundColor(CINZA);
                            grupo.setPadding(5);
                            tabela.addCell(grupo);
                        }
                        celula(tabela, "" + p.getId(), Element.ALIGN_CENTER);
                        celula(tabela, p.getClienteNome(), Element.ALIGN_LEFT);
                        celula(tabela, p.getClienteTelefone(), Element.ALIGN_LEFT);
                        celula(tabela, texto(p.getTipoPagamento()), Element.ALIGN_LEFT);
                        celula(tabela, texto(p.getStatus()), Element.ALIGN_LEFT);
                        celula(tabela, Formato.valor(p.getTotal()), Element.ALIGN_RIGHT);
                        subtotal += p.getTotal();
                        totalGeral += p.getTotal();
                    }
                    linhaTotal(tabela, "Subtotal do dia", Formato.valor(subtotal), 6);
                    doc.add(tabela);

                    Paragraph resumo = new Paragraph(pedidos.size() + " pedidos  |  Valor total: " + Formato.moeda(totalGeral), SUBTITULO);
                    resumo.setAlignment(Element.ALIGN_RIGHT);
                    resumo.setSpacingBefore(10);
                    doc.add(resumo);

                    doc.close();
                    abrir(arquivo);
                } catch (Exception e) {
                    Mensagem.erro("Erro ao gerar o PDF: " + e.getMessage());
                }
            }
        }
    }

    // ------------------------------------------------------------ Auxiliares

    private static File escolherArquivo(Window janela, String nomeSugerido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar relatório em PDF");
        chooser.setInitialFileName(nomeSugerido);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        File documentos = new File(System.getProperty("user.home"), "Documents");
        if (documentos.isDirectory())
            chooser.setInitialDirectory(documentos);
        File arquivo = chooser.showSaveDialog(janela);
        if (arquivo != null && !arquivo.getName().toLowerCase().endsWith(".pdf"))
            arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".pdf");
        return arquivo;
    }

    private static Document abrirDocumento(File arquivo) throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(arquivo));
        doc.open();
        return doc;
    }

    private static void cabecalho(Document doc, String titulo) throws Exception {
        Paragraph empresa = new Paragraph("MANÉ DELIVERY", TITULO);
        doc.add(empresa);
        doc.add(new Paragraph("Delivery de bebidas", PEQUENO));
        String emissao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        Paragraph data = new Paragraph("Emitido em " + emissao, PEQUENO);
        data.setAlignment(Element.ALIGN_RIGHT);
        doc.add(data);
        Paragraph t = new Paragraph(titulo, SUBTITULO);
        t.setSpacingBefore(10);
        t.setSpacingAfter(8);
        doc.add(t);
    }

    private static void titulos(PdfPTable tabela, String... nomes) {
        for (int i = 0; i < nomes.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(nomes[i], NEGRITO));
            c.setBackgroundColor(new Color(200, 200, 200));
            c.setPadding(5);
            tabela.addCell(c);
        }
        tabela.setHeaderRows(1);
    }

    private static void celula(PdfPTable tabela, String texto, int alinhamento) {
        PdfPCell c = new PdfPCell(new Phrase(texto, NORMAL));
        c.setHorizontalAlignment(alinhamento);
        c.setPadding(4);
        tabela.addCell(c);
    }

    private static void linhaDado(PdfPTable tabela, String rotulo, String valor) {
        PdfPCell r = new PdfPCell(new Phrase(rotulo, NEGRITO));
        r.setBorder(Rectangle.NO_BORDER);
        r.setPadding(3);
        PdfPCell v = new PdfPCell(new Phrase(valor, NORMAL));
        v.setBorder(Rectangle.NO_BORDER);
        v.setPadding(3);
        tabela.addCell(r);
        tabela.addCell(v);
    }

    private static void linhaTotal(PdfPTable tabela, String rotulo, String valor, int colunas) {
        PdfPCell r = new PdfPCell(new Phrase(rotulo, NEGRITO));
        r.setColspan(colunas - 1);
        r.setHorizontalAlignment(Element.ALIGN_RIGHT);
        r.setPadding(5);
        PdfPCell v = new PdfPCell(new Phrase(valor, NEGRITO));
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setPadding(5);
        tabela.addCell(r);
        tabela.addCell(v);
    }

    private static String texto(Object o) {
        String t = "-";
        if (o != null)
            t = o.toString();
        return t;
    }

    // Abre o PDF no programa padrão do Windows
    private static void abrir(File arquivo) {
        Thread t = new Thread(() -> {
            try {
                Desktop.getDesktop().open(arquivo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
