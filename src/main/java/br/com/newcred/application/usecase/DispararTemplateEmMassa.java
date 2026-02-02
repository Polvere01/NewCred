package br.com.newcred.application.usecase;

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
    public DisparoResultadoDto executar(String template, MultipartFile file, String phoneNumberId) {
        String filename = (file.getOriginalFilename() == null) ? "" : file.getOriginalFilename().toLowerCase();

        if (filename.endsWith(".csv")) {
            return processarCsv(template, file, phoneNumberId);
        }

        // default: tenta xlsx (ou qualquer coisa excel)
        return processarXlsx(template, file, phoneNumberId);
    }

    //TODO TENTAR ENTENDER DEPOIS DIFICIL DEMAIS
    public DisparoResultadoDto processarXlsx(String template, MultipartFile file, String phoneNumberId) {
        var erros = new ArrayList<FalhaDto>();
        int total = 0;
        int enviados = 0;

        try (var in = file.getInputStream();
             var wb = WorkbookFactory.create(in)) {

            var sheet = wb.getSheetAt(0);
            var formatter = new DataFormatter();

            // headers
            var header = sheet.getRow(0);
            if (header == null) throw new IllegalArgumentException("Planilha sem header (linha 1)");

            int colTelefone = -1;
            int colNome = -1;
            int colCpf = -1;
            int colValorContrato = -1;

            for (Cell c : header) {
                String h = formatter.formatCellValue(c).trim().toLowerCase();

                // aceita variações pra reaproveitar em outras planilhas
                if (h.equals("telefone") || h.equals("telefone1")) colTelefone = c.getColumnIndex();
                if (h.equals("nome")) colNome = c.getColumnIndex();
                if (h.equals("cpf")) colCpf = c.getColumnIndex();
                if (h.equals("valorcontrato") || h.equals("valor_contrato") || h.equals("valor contrato"))
                    colValorContrato = c.getColumnIndex();
            }

            if (colTelefone < 0 || colNome < 0) {
                throw new IllegalArgumentException("Cabeçalho precisa ter: telefone e nome");
            }
            if (colCpf < 0 || colValorContrato < 0) {
                throw new IllegalArgumentException("Cabeçalho precisa ter: cpf e valorContrato");
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                var row = sheet.getRow(r);
                if (row == null) continue;

                String telRaw = formatter.formatCellValue(row.getCell(colTelefone)).trim();
                String nomeRaw = formatter.formatCellValue(row.getCell(colNome)).trim();
                String cpfRaw = formatter.formatCellValue(row.getCell(colCpf)).trim();
                String valorContratoRaw = formatter.formatCellValue(row.getCell(colValorContrato)).trim();

                if (telRaw.isBlank() || nomeRaw.isBlank()) continue;

                total++;

                String telefone = normalizarTelefoneBR(telRaw);
                String primeiroNome = primeiroNome(nomeRaw);

                // cpf só dígitos (sem ponto/traço)
                String cpf = cpfRaw.replaceAll("\\D", "");
                // valorContrato: deixa como vem (ex: "4.153,69") ou normaliza depois
                String valorContrato = valorContratoRaw;

                if (telefone == null) {
                    erros.add(new FalhaDto(telRaw, nomeRaw, "Telefone inválido"));
                    continue;
                }
                if (cpf.isBlank()) {
                    erros.add(new FalhaDto(telefone, nomeRaw, "CPF vazio/inválido"));
                    continue;
                }
                if (valorContrato.isBlank()) {
                    erros.add(new FalhaDto(telefone, nomeRaw, "valorContrato vazio"));
                    continue;
                }

                try {
                    // ✅ SEM HARDCODE: usa o phoneNumberId recebido
                    // ✅ aqui assume que teu client vai aceitar cpf e valorContrato
                    waClient.enviarTemplate(template, telefone, primeiroNome, cpf, valorContrato, phoneNumberId);
                    enviados++;

                    Thread.sleep(5000); // ajusta depois
                } catch (Exception ex) {
                    erros.add(new FalhaDto(telefone, nomeRaw, ex.getMessage()));
                }
            }

            return new DisparoResultadoDto(total, enviados, erros.size(), erros);

        } catch (Exception e) {
            throw new RuntimeException("Falha no disparo (XLSX)", e);
        }
    }

    private DisparoResultadoDto processarCsv(String template, MultipartFile file, String phoneNumberId) {
        var erros = new ArrayList<FalhaDto>();
        int total = 0;
        int enviados = 0;

        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {

            var csv = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader()
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
                    System.out.println(" Disparando para " + telefone + " (" + primeiroNome + ")");
                    waClient.enviarTemplate(template, telefone, primeiroNome, phoneNumberId);
                    enviados++;
                    Thread.sleep(5000)
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
