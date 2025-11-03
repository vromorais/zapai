package br.com.zapai.atendimento.dominio;

import br.com.zapai.atendimento.dominio.enums.StatusAgendamento;
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
@Table(name = "agendamento")
public class Agendamento {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(name = "inicio_em", nullable = false)
    private OffsetDateTime inicioEm;

    @Column(name = "fim_em", nullable = false)
    private OffsetDateTime fimEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status;

    @Column(name = "id_evento_google", length = 255)
    private String idEventoGoogle;

    @Column(columnDefinition = "text")
    private String observacoes;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Clinica getClinica() {
        return clinica;
    }

    public void setClinica(Clinica clinica) {
        this.clinica = clinica;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
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

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public String getIdEventoGoogle() {
        return idEventoGoogle;
    }

    public void setIdEventoGoogle(String idEventoGoogle) {
        this.idEventoGoogle = idEventoGoogle;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
