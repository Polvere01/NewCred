package br.com.newcred.application.usecase;

import br.com.newcred.application.usecase.dto.MensagensRequestDto;
import br.com.newcred.application.usecase.dto.MensagensResponseDto;
import br.com.newcred.application.usecase.port.IEnviarMensagem;
import br.com.newcred.application.usecase.port.IMensagemRepository;
import br.com.newcred.application.usecase.port.IWhatsAppCloudApiClient;
import org.springframework.stereotype.Service;

@Service
public class EnviarMensagem implements IEnviarMensagem {

    private final IWhatsAppCloudApiClient client;
    private final IMensagemRepository mensagemRepository;

    public EnviarMensagem(IWhatsAppCloudApiClient client, IMensagemRepository mensagemRepository) {
        this.client = client;
        this.mensagemRepository = mensagemRepository;
    }

    @Override
    public MensagensResponseDto enviar(MensagensRequestDto dto, String phoneNumberId) {

        String wamid = client.enviarTexto(phoneNumberId ,dto.waIdDestino(), dto.texto());

        mensagemRepository.salvarSaida(dto.conversaId(), wamid, dto.waIdDestino(), dto.texto(), phoneNumberId);

        return new MensagensResponseDto(wamid);
    }
}
