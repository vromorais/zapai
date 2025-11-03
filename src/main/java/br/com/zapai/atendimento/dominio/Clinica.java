package br.com.zapai.atendimento.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "clinica")
public class Clinica {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "fuso_horario", nullable = false, length = 64)
    private String fusoHorario = "America/Sao_Paulo";

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }
}
