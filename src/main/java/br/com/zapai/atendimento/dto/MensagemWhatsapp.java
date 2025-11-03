package br.com.zapai.atendimento.dto;

import java.util.Map;

public class MensagemWhatsapp {

    private final String telefone;
    private final String mensagem;
    private final String idMensagem;
    private final Map<String, Object> dadosOriginais;

    public MensagemWhatsapp(String telefone, String mensagem, String idMensagem, Map<String, Object> dadosOriginais) {
        this.telefone = telefone;
        this.mensagem = mensagem;
        this.idMensagem = idMensagem;
        this.dadosOriginais = dadosOriginais;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getIdMensagem() {
        return idMensagem;
    }

    public Map<String, Object> getDadosOriginais() {
        return dadosOriginais;
    }
}
