package br.com.newcred.domain.model;


public class Contato {

    private final Long id;
    private final String whatsappId;
    private final String nome;

    private Contato(Long id, String whatsappId, String nome) {
        this.id = id;
        this.whatsappId = whatsappId;
        this.nome = nome;
    }

    // Para contato NOVO (ainda não persistido)
    public static Contato novo(String whatsappId, String nome) {
        return new Contato(null, whatsappId, nome);
    }

    // Para contato EXISTENTE (vindo do banco)
    public static Contato existente(Long id, String whatsappId, String nome) {
        return new Contato(id, whatsappId, nome);
    }

    public Long getId() {
        return id;
    }

    public String getWhatsappId() {
        return whatsappId;
    }

    public String getNome() {
        return nome;
    }
}
