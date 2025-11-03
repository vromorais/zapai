package br.com.zapai.atendimento.servico;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessaoCliente implements Serializable {

    @JsonProperty("telefone")
    private String telefone;

    @JsonProperty("clinica_id")
    private UUID clinicaId;

    @JsonProperty("profissional_id")
    private UUID profissionalId;

    @JsonProperty("paciente_id")
    private UUID pacienteId;

    @JsonProperty("contexto")
    private Map<String, Object> contexto = new HashMap<>();

    @JsonProperty("atualizado_em")
    private OffsetDateTime atualizadoEm;

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public UUID getClinicaId() {
        return clinicaId;
    }

    public void setClinicaId(UUID clinicaId) {
        this.clinicaId = clinicaId;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public void setProfissionalId(UUID profissionalId) {
        this.profissionalId = profissionalId;
    }

    public UUID getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(UUID pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Map<String, Object> getContexto() {
        return contexto;
    }

    public void setContexto(Map<String, Object> contexto) {
        this.contexto = contexto;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(OffsetDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
