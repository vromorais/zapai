package br.com.zapai.atendimento.dominio;

import br.com.zapai.atendimento.dominio.enums.StatusDisponibilidade;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "disponibilidade")
public class Disponibilidade {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @Column(name = "inicio_em", nullable = false)
    private OffsetDateTime inicioEm;

    @Column(name = "fim_em", nullable = false)
    private OffsetDateTime fimEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDisponibilidade status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public OffsetDateTime getInicioEm() {
        return inicioEm;
    }

    public void setInicioEm(OffsetDateTime inicioEm) {
        this.inicioEm = inicioEm;
    }

    public OffsetDateTime getFimEm() {
        return fimEm;
    }

    public void setFimEm(OffsetDateTime fimEm) {
        this.fimEm = fimEm;
    }

    public StatusDisponibilidade getStatus() {
        return status;
    }

    public void setStatus(StatusDisponibilidade status) {
        this.status = status;
    }
}
