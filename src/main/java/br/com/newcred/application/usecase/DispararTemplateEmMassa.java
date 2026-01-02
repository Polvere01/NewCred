package br.com.newcred.application.usecase;

import br.com.newcred.adapters.meta.WhatsAppCloudClient;
import br.com.newcred.application.usecase.dto.DisparoResultadoDto;
import br.com.newcred.application.usecase.dto.FalhaDto;
import br.com.newcred.application.usecase.port.IDispararTemplateEmMassa;
import br.com.newcred.application.usecase.port.IWhatsAppCloudClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Service
public class DispararTemplateEmMassa implements IDispararTemplateEmMassa {

    private final IWhatsAppCloudClient waClient;

    public DispararTemplateEmMassa(IWhatsAppCloudClient waClient) {
        this.waClient = waClient;
    }

    @Override
    public DisparoResultadoDto executar(String template, MultipartFile file) {
        String filename = (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".csv")) {
            return processarCsv(template, file);
        }

        // default: tenta xlsx (ou qualquer coisa excel)
        return processarXlsx(template, file);
    }

    //TODO TENTAR ENTENDER DEPOIS DIFICIL DEMAIS
    public DisparoResultadoDto processarXlsx(String template, MultipartFile file) {
        var erros = new ArrayList<FalhaDto>();
        int total = 0;
        int enviados = 0;

        try (var in = file.getInputStream();
             var wb = WorkbookFactory.create(in)) {

            var sheet = wb.getSheetAt(0);
            var formatter = new DataFormatter();

            // acha colunas pelo header (linha 0)
            var header = sheet.getRow(0);
            int colTelefone = -1;
            int colNome = -1;

            for (Cell c : header) {
                String h = formatter.formatCellValue(c).trim().toUpperCase();
                if (h.equals("TELEFONE1")) colTelefone = c.getColumnIndex();
                if (h.equals("NOME")) colNome = c.getColumnIndex();
            }

            if (colTelefone < 0 || colNome < 0) {
                throw new IllegalArgumentException("Cabeçalho precisa ter TELEFONE1 e NOME");
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                var row = sheet.getRow(r);
                if (row == null) continue;

                String telRaw = formatter.formatCellValue(row.getCell(colTelefone)).trim();
                String nomeRaw = formatter.formatCellValue(row.getCell(colNome)).trim();

                if (telRaw.isBlank() || nomeRaw.isBlank()) continue;

                total++;

                String telefone = normalizarTelefoneBR(telRaw);
                String primeiroNome = primeiroNome(nomeRaw);

                if (telefone == null) {
                    erros.add(new FalhaDto(telRaw, nomeRaw, "Telefone inválido"));
                    continue;
                }

                try {
                    waClient.enviarTemplate(template, telefone, primeiroNome);
                    enviados++;

                    // 👉 opcional: “freio” simples pra não estourar limite
                    Thread.sleep(120); // ajuste depois
                } catch (Exception ex) {
                    erros.add(new FalhaDto(telefone, nomeRaw, ex.getMessage()));
                }
            }

            return new DisparoResultadoDto(total, enviados, erros.size(), erros);

        } catch (Exception e) {
            throw new RuntimeException("Falha no disparo", e);
        }
    }

    private DisparoResultadoDto processarCsv(String template, MultipartFile file) {
        var erros = new ArrayList<FalhaDto>();
        int total = 0;
        int enviados = 0;

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {

            var csv = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()                  // pega header automaticamente da primeira linha
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            // valida cabeçalho
            if (!csv.getHeaderMap().containsKey("TELEFONE1") || !csv.getHeaderMap().containsKey("NOME")) {
                throw new IllegalArgumentException("CSV precisa ter colunas TELEFONE1 e NOME");
            }

            for (var rec : csv) {
                String telRaw = rec.get("TELEFONE1");
                String nomeRaw = rec.get("NOME");

                if (telRaw == null || nomeRaw == null) continue;

                telRaw = telRaw.trim();
                nomeRaw = nomeRaw.trim();

                if (telRaw.isBlank() || nomeRaw.isBlank()) continue;

                total++;

                String telefone = normalizarTelefoneBR(telRaw);
                String primeiroNome = primeiroNome(nomeRaw);

                if (telefone == null) {
                    erros.add(new FalhaDto(telRaw, nomeRaw, "Telefone inválido"));
                    continue;
                }

                try {
                    waClient.enviarTemplate(template, telefone, primeiroNome);
                    enviados++;
                    Thread.sleep(120);
                } catch (Exception ex) {
                    erros.add(new FalhaDto(telefone, nomeRaw, ex.getMessage()));
                }
            }

            return new DisparoResultadoDto(total, enviados, erros.size(), erros);

        } catch (Exception e) {
            throw new RuntimeException("Falha no disparo (CSV)", e);
        }
    }


    private static String primeiroNome(String nome) {
        var parts = nome.trim().split("\\s+");
        return parts.length == 0 ? "" : parts[0];
    }

    private static String normalizarTelefoneBR(String input) {
        // remove tudo que não é dígito
        String digits = input.replaceAll("\\D", "");

        // se veio em notação científica, digits pode vir truncado; por isso o DataFormatter ajuda
        // regras simples BR:
        // - se tiver 11 (DDD+9+8) -> prefixa 55
        // - se já tiver 13 com 55 -> ok
        if (digits.length() == 11) return "55" + digits;
        if (digits.length() == 13 && digits.startsWith("55")) return digits;

        // algumas planilhas vem com 10 (sem 9) - decide o que fazer:
        // if (digits.length()==10) return "55" + digits; // se quiser aceitar
        return null;
    }
}