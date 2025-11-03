package br.com.zapai.atendimento.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "log_mensagem")
public class LogMensagem {

    @Id
    private UUID id;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(nullable = false, length = 10)
    private String direcao;

    @Column(name = "telefone_e164", length = 20)
    private String telefoneE164;

    @Column(columnDefinition = "jsonb")
    private String payload;

    @Column(name = "criado_em", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    @Column(name = "id_sessao", length = 64)
    private String idSessao;

    @Column(length = 50)
    private String intent;

    @Column(name = "id_correlacao", length = 64)
    private String idCorrelacao;

    @Version
    private Long versao;

    @Transient
    private boolean processado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getDirecao() {
        return direcao;
    }

    public void setDirecao(String direcao) {
        this.direcao = direcao;
    }

    public String getTelefoneE164() {
        return telefoneE164;
    }

    public void setTelefoneE164(String telefoneE164) {
        this.telefoneE164 = telefoneE164;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getIdSessao() {
        return idSessao;
    }

    public void setIdSessao(String idSessao) {
        this.idSessao = idSessao;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getIdCorrelacao() {
        return idCorrelacao;
    }

    public void setIdCorrelacao(String idCorrelacao) {
        this.idCorrelacao = idCorrelacao;
    }

    public Long getVersao() {
        return versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }

    public boolean isProcessado() {
        return processado;
    }

    public void setProcessado(boolean processado) {
        this.processado = processado;
    }
}
