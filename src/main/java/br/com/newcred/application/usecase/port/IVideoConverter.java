package br.com.newcred.application.usecase.port;

import br.com.newcred.application.usecase.dto.MediaConvertResult;
import org.springframework.web.multipart.MultipartFile;

public interface IVideoConverter {
    MediaConvertResult toMp4H264Aac(MultipartFile input);
}
