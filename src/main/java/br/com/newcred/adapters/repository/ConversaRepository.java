package br.com.newcred.adapters.repository;

import br.com.newcred.application.usecase.port.IConversaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public class ConversaRepository implements IConversaRepository {


    private final JdbcTemplate jdbc;

    public ConversaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public long criarOuAtualizar(long contatoId, OffsetDateTime ultimaMensagemEm) {
        String sql = """
            insert into conversas (contato_id, status, ultima_mensagem_em)
            values (?, 'ABERTA', ?)
            on conflict (contato_id)
            do update set
              ultima_mensagem_em = excluded.ultima_mensagem_em,
              atualizado_em = now()
            returning id
        """;

        Long id = jdbc.queryForObject(sql, Long.class, contatoId, ultimaMensagemEm);
        return id != null ? id : -1L;
    }

    @Override
    public void atribuirOperadorSeNulo(long conversaId) {

        // 1) trava a linha da conversa e pega operador atual
        Long operadorAtual = jdbc.queryForObject("""
            select operador_id
            from conversas
            where id = ?
            for update
        """, Long.class, conversaId);

        if (operadorAtual != null) return; // já atribuído

        // 2) escolhe próximo operador (round-robin por last_assigned_at)
        Long opId = jdbc.queryForObject("""
            select id
            from operadores
            where ativo = true
              and role = 'OPERADOR'
            order by last_assigned_at nulls first, id
            limit 1
            for update skip locked
        """, Long.class);

        if (opId == null) return; // sem operador disponível

        // 3) seta operador na conversa (só se ainda estiver null)
        int updated = jdbc.update("""
            update conversas
            set operador_id = ?, atualizado_em = now()
            where id = ?
              and operador_id is null
        """, opId, conversaId);

        if (updated == 0) return; // alguém setou antes

        // 4) marca operador como "último escolhido"
        jdbc.update("""
            update operadores
            set last_assigned_at = now(), updated_at = now()
            where id = ?
        """, opId);
    }
}
