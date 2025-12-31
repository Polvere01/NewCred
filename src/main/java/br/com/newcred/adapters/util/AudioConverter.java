package br.com.newcred.adapters.util;

import br.com.newcred.application.usecase.port.IAudioConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class AudioConverter implements IAudioConverter {

    @Override
    public AudioConvertResult webmToOggOpus(MultipartFile input) {

        try {
            // cria arquivo temporário pro webm
            Path tempWebm = Files.createTempFile("audio-input-", ".webm");
            input.transferTo(tempWebm.toFile());

            // cria arquivo temporário pro ogg/opus
            Path tempOgg = Files.createTempFile("audio-output-", ".ogg");

            // converte usando ffmpeg
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-hide_banner",
                    "-loglevel", "error",
                    "-y",
                    "-i", tempWebm.toString(),
                    "-vn",
                    "-ac", "1",
                    "-c:a", "libopus",
                    "-b:a", "24k",
                    "-application", "voip",
                    "-frame_duration", "20",
                    "-af", "aresample=async=1:first_pts=0",
                    "-f", "ogg",
                    tempOgg.toString()
            );

            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Erro na conversão de áudio, código de saída: " + exitCode);
            }

            // lê bytes do arquivo convertido
            byte[] oggBytes = Files.readAllBytes(tempOgg);

            // deleta arquivos temporários
            Files.deleteIfExists(tempWebm);
            Files.deleteIfExists(tempOgg);

            return new AudioConvertResult(oggBytes, "audio/ogg", "audio.opus");

        } catch (Exception e) {
            throw new RuntimeException("Falha ao converter áudio de webm para ogg/opus", e);
        }
    }
}
