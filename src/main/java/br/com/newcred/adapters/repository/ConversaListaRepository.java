package br.com.newcred.adapters.repository;

import br.com.newcred.adapters.dto.ConversaListaDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ConversaListaRepository {

    private final JdbcTemplate jdbc;

    public ConversaListaRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ConversaListaDTO> listar() {
        String sql = """
            select
              c.id as id,
              ct.whatsapp_id as nome,
              coalesce(m.texto, '') as "ultimaMensagem"
            from conversas c
            join contatos ct on ct.id = c.contato_id
            left join lateral (
              select texto, enviado_em
              from mensagens
              where conversa_id = c.id
              order by enviado_em desc
              limit 1
            ) m on true
            order by c.ultima_mensagem_em desc nulls last, c.atualizado_em desc
        """;

        return jdbc.query(sql, (rs, i) ->
                new ConversaListaDTO(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("ultimaMensagem")
                )
        );
    }

    public List<ConversaListaDTO> listarPorOperador(long operadorId) {
        String sql = """
            select
              c.id as id,
              ct.whatsapp_id as nome,
              coalesce(m.texto, '') as "ultimaMensagem"
            from conversas c
            join contatos ct on ct.id = c.contato_id
            left join lateral (
              select texto, enviado_em
              from mensagens
              where conversa_id = c.id
              order by enviado_em desc
              limit 1
            ) m on true
            where c.operador_id = ?
            order by c.ultima_mensagem_em desc nulls last, c.atualizado_em desc
        """;

        return jdbc.query(sql, (rs, i) ->
                        new ConversaListaDTO(
                                rs.getLong("id"),
                                rs.getString("nome"),
                                rs.getString("ultimaMensagem")
                        ),
                operadorId
        );
    }
}