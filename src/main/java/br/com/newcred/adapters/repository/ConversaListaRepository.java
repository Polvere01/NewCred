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
                  coalesce(m.texto, '') as "ultimaMensagem",
                  coalesce(o.nome, '') as "operadorNome",
                  coalesce(m.direcao, '') as "ultimaDirecao",
                  coalesce(m.tipo, '') as "ultimaTipo",
                  coalesce(m.phone_number_id, '') as "phoneNumberId",
                  c.tag_label as "tagLabel",
                  c.tag_color as "tagColor"
                from conversas c
                join contatos ct on ct.id = c.contato_id
                left join operadores o on o.id = c.operador_id
                left join lateral (
                  select
                    texto,
                    enviado_em,
                    direcao,
                    tipo,
                    phone_number_id
                  from mensagens
                  where conversa_id = c.id
                  order by enviado_em desc, id desc
                  limit 1
                ) m on true
                order by c.ultima_mensagem_em desc nulls last, c.atualizado_em desc
                """;

        return jdbc.query(sql, (rs, i) ->
                new ConversaListaDTO(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("ultimaMensagem"),
                        rs.getString("operadorNome"),
                        dirApi(rs.getString("ultimaDirecao")),
                        rs.getString("ultimaTipo"),
                        rs.getString("phoneNumberId"),
                        rs.getString("tagLabel"),
                        rs.getString("tagColor")
                )
        );
    }

    public List<ConversaListaDTO> listarPorOperador(long operadorId) {
        String sql = """
                    select
                      c.id as id,
                      ct.whatsapp_id as nome,
                      coalesce(m.texto, '') as "ultimaMensagem",
                      coalesce(o.nome, '') as "operadorNome",
                      coalesce(m.direcao, '') as "ultimaDirecao",
                      coalesce(m.tipo, '') as "ultimaTipo",
                      coalesce(m.phone_number_id, '') as "phoneNumberId",
                      c.tag_label as "tagLabel",
                      c.tag_color as "tagColor"
                    from conversas c
                    join contatos ct on ct.id = c.contato_id
                    left join operadores o on o.id = c.operador_id
                    left join lateral (
                      select texto, enviado_em, direcao, tipo,
                      phone_number_id
                      from mensagens
                      where conversa_id = c.id
                      order by enviado_em desc, id desc
                      limit 1
                    ) m on true
                    where c.operador_id = ?
                    order by c.ultima_mensagem_em desc nulls last, c.atualizado_em desc
                """;

        return jdbc.query(sql, (rs, i) ->
                        new ConversaListaDTO(
                                rs.getLong("id"),
                                rs.getString("nome"),
                                rs.getString("ultimaMensagem"),
                                null,
                                dirApi(rs.getString("ultimaDirecao")),
                                rs.getString("ultimaTipo"),
                                rs.getString("phoneNumberId"),
                                rs.getString("tagLabel"),
                                rs.getString("tagColor")
                        ),
                operadorId
        );
    }

    public List<ConversaListaDTO> listarPorSupervisor(long supervisorId) {
        String sql = """
                    select
                      c.id as id,
                      ct.whatsapp_id as nome,
                      coalesce(m.texto, '') as "ultimaMensagem",
                      coalesce(o.nome, '') as "operadorNome",
                      coalesce(m.direcao, '') as "ultimaDirecao",
                      coalesce(m.tipo, '') as "ultimaTipo",
                      coalesce(m.phone_number_id, '') as "phoneNumberId",
                      c.tag_label as "tagLabel",
                      c.tag_color as "tagColor"
                    from conversas c
                    join contatos ct on ct.id = c.contato_id
                    join operadores o on o.id = c.operador_id
                    left join lateral (
                      select texto, enviado_em, direcao, tipo,
                      phone_number_id
                      from mensagens
                      where conversa_id = c.id
                      order by enviado_em desc, id desc
                      limit 1
                    ) m on true
                    where o.supervisor_id = ?
                       or c.operador_id = ?
                    order by c.ultima_mensagem_em desc nulls last, c.atualizado_em desc
                """;

        return jdbc.query(sql, (rs, i) ->
                        new ConversaListaDTO(
                                rs.getLong("id"),
                                rs.getString("nome"),
                                rs.getString("ultimaMensagem"),
                                rs.getString("operadorNome"),
                                dirApi(rs.getString("ultimaDirecao")),
                                rs.getString("ultimaTipo"),
                                rs.getString("phoneNumberId"),
                                rs.getString("tagLabel"),
                                rs.getString("tagColor")
                        ),
                supervisorId, supervisorId
        );
    }

    public void atualizarTag(long conversaId, String label, String color) {
        String sql = """
                  update conversas
                  set tag_label = ?, tag_color = ?, atualizado_em = now()
                  where id = ?
                """;
        jdbc.update(sql, label, color, conversaId);
    }

    private static String dirApi(String dirDb) {
        if (dirDb == null) return "";
        return "IN".equalsIgnoreCase(dirDb) ? "ENTRADA" : "SAIDA";
    }
}