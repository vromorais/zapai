package br.com.zapai.atendimento.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinica_id", nullable = false)
    private Clinica clinica;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(columnDefinition = "text")
    private String especialidades;

    @Column(name = "calendario_id", length = 255)
    private String calendarioId;

    @Column(name = "minutos_slot_padrao")
    private Integer minutosSlotPadrao = 30;

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

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public String getCalendarioId() {
        return calendarioId;
    }

    public void setCalendarioId(String calendarioId) {
        this.calendarioId = calendarioId;
    }

    public Integer getMinutosSlotPadrao() {
        return minutosSlotPadrao;
    }

    public void setMinutosSlotPadrao(Integer minutosSlotPadrao) {
        this.minutosSlotPadrao = minutosSlotPadrao;
    }
}
