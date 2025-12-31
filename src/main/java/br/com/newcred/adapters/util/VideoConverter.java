package br.com.newcred.adapters.util;

import br.com.newcred.application.usecase.dto.MediaConvertResult;
import br.com.newcred.application.usecase.port.IVideoConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class VideoConverter implements IVideoConverter {

    @Override
    public MediaConvertResult toMp4H264Aac(MultipartFile input) {
        try {
            Path tempIn = Files.createTempFile("video-input-", ".webm"); // pode ser qualquer extensão, tanto faz
            input.transferTo(tempIn.toFile());

            Path tempOut = Files.createTempFile("video-output-", ".mp4");

            ProcessBuilder pb = getProcessBuilder(tempIn, tempOut);
            int exit = pb.start().waitFor();
            if (exit != 0) throw new RuntimeException("Erro na conversão de vídeo, exit=" + exit);

            byte[] mp4Bytes = Files.readAllBytes(tempOut);

            Files.deleteIfExists(tempIn);
            Files.deleteIfExists(tempOut);

            return new MediaConvertResult(mp4Bytes, "video/mp4", "video.mp4");
        } catch (Exception e) {
            throw new RuntimeException("Falha ao converter vídeo para mp4/h264/aac", e);
        }
    }

    private static ProcessBuilder getProcessBuilder(Path tempIn, Path tempOut) {
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-hide_banner",
                "-loglevel", "error",
                "-y",
                "-i", tempIn.toString(),

                // vídeo
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                "-profile:v", "baseline",
                "-level", "3.1",
                "-preset", "veryfast",
                "-crf", "28",

                // áudio (WhatsApp gosta de AAC em mp4)
                "-c:a", "aac",
                "-b:a", "96k",
                "-ac", "1",
                "-ar", "48000",

                // move moov pro começo (streaming / compatibilidade)
                "-movflags", "+faststart",

                tempOut.toString()
        );

        pb.inheritIO();
        return pb;
    }
}