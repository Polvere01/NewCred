package br.com.newcred.adapters.jobs;

import br.com.newcred.application.usecase.port.IEventoWebhookRepository;
import org.springframework.stereotype.Component;
import br.com.newcred.application.usecase.ProcessarEventoWebhook;
import br.com.newcred.domain.model.EventoWebhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ProcessadorEventosWebhook {
    private static final Logger log = LoggerFactory.getLogger(ProcessadorEventosWebhook.class);

    private final IEventoWebhookRepository repo;
    private final ProcessarEventoWebhook processarUseCase;

    public ProcessadorEventosWebhook(IEventoWebhookRepository repo,
                                              ProcessarEventoWebhook processarUseCase) {
        this.repo = repo;
        this.processarUseCase = processarUseCase;
    }

    // a cada 2 segundos (ajusta como quiser)
    @Scheduled(fixedDelay = 1000)
    public void executar() {
        // pega um lote por rodada
        List<EventoWebhook> eventos = buscarLote(20);
        if (eventos.isEmpty()) return;

        for (EventoWebhook e : eventos) {
            processarUm(e);
        }
    }

    @Transactional
    protected List<EventoWebhook> buscarLote(int limite) {
        return repo.buscarPendentesParaProcessar(limite);
    }

    @Transactional
    protected void processarUm(EventoWebhook evento) {
        try {
            processarUseCase.executar(evento);
            repo.marcarProcessado(evento.getId());
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null) msg = ex.getClass().getSimpleName();
            // corta pra não estourar coluna (se for text, pode deixar)
            if (msg.length() > 2000) msg = msg.substring(0, 2000);

            log.error("Erro processando evento id={}: {}", evento.getId(), msg, ex);
            repo.marcarErro(evento.getId(), msg);
        }
    }
}
