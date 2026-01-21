package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IPhoneNumberMetaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PhoneNumberMetaRepository implements IPhoneNumberMetaRepository {

    private final JdbcTemplate jdbc;

    public PhoneNumberMetaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public TokenData buscarAtivo(String phoneNumberId) {
        String sql = """
            select meta_token_enc, meta_token_nonce
            from phone_number_meta
            where phone_number_id = ?
              and active = true
            limit 1
        """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) {
                throw new RuntimeException(
                        "Token não configurado para phone_number_id=" + phoneNumberId
                );
            }
            return new TokenData(
                    rs.getString("meta_token_enc"),
                    rs.getString("meta_token_nonce")
            );
        }, phoneNumberId);
    }

    @Override
    public void upsert(String phoneNumberId, String encB64, String nonceB64, boolean active) {
        String sql = """
            insert into phone_number_meta
              (phone_number_id, meta_token_enc, meta_token_nonce, active)
            values (?, ?, ?, ?)
            on conflict (phone_number_id) do update set
              meta_token_enc = excluded.meta_token_enc,
              meta_token_nonce = excluded.meta_token_nonce,
              active = excluded.active,
              updated_at = now()
        """;

        jdbc.update(sql, phoneNumberId, encB64, nonceB64, active);
    }
}
