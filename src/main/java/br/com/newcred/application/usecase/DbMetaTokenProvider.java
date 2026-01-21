package br.com.newcred.application.usecase;

import br.com.newcred.adapters.config.TokenCrypto;
import br.com.newcred.application.usecase.port.IMetaTokenProvider;
import br.com.newcred.application.usecase.port.IPhoneNumberMetaRepository;
import org.springframework.stereotype.Service;

@Service
public class DbMetaTokenProvider implements IMetaTokenProvider {

    private final IPhoneNumberMetaRepository repo;
    private final TokenCrypto crypto;

    public DbMetaTokenProvider(
            IPhoneNumberMetaRepository repo,
            TokenCrypto crypto
    ) {
        this.repo = repo;
        this.crypto = crypto;
    }

    @Override
    public String getToken(String phoneNumberId) {
        var data = repo.buscarAtivo(phoneNumberId);
        return crypto.decrypt(data.encB64(), data.nonceB64());
    }
}
