package br.com.zapai.atendimento.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanoAcao {

    @JsonProperty("tipo")
    private TipoPlanoAcao tipo;

    @JsonProperty("resposta_usuario")
    private String respostaUsuario;

    @JsonProperty("perguntas_pendentes")
    private List<String> perguntasPendentes;

    @JsonProperty("dados_agendamento")
    private DadosAgendamento dadosAgendamento;

    @JsonProperty("metadados")
    private Map<String, Object> metadados;

    public TipoPlanoAcao getTipo() {
        return tipo;
    }

    public void setTipo(TipoPlanoAcao tipo) {
        this.tipo = tipo;
    }

    public String getRespostaUsuario() {
        return respostaUsuario;
    }

    public void setRespostaUsuario(String respostaUsuario) {
        this.respostaUsuario = respostaUsuario;
    }

    public List<String> getPerguntasPendentes() {
        return perguntasPendentes;
    }

    public void setPerguntasPendentes(List<String> perguntasPendentes) {
        this.perguntasPendentes = perguntasPendentes;
    }

    public DadosAgendamento getDadosAgendamento() {
        return dadosAgendamento;
    }

    public void setDadosAgendamento(DadosAgendamento dadosAgendamento) {
        this.dadosAgendamento = dadosAgendamento;
    }

    public Map<String, Object> getMetadados() {
        return metadados;
    }

    public void setMetadados(Map<String, Object> metadados) {
        this.metadados = metadados;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DadosAgendamento {

        @JsonProperty("clinica_id")
        private UUID clinicaId;

        @JsonProperty("profissional_id")
        private UUID profissionalId;

        @JsonProperty("paciente_id")
        private UUID pacienteId;

        @JsonProperty("nome_paciente")
        private String nomePaciente;

        @JsonProperty("telefone_e164")
        private String telefoneE164;

        @JsonProperty("inicio_em")
        private OffsetDateTime inicioEm;

        @JsonProperty("fim_em")
        private OffsetDateTime fimEm;

        @JsonProperty("observacoes")
        private String observacoes;

        @JsonProperty("id_agendamento")
        private UUID idAgendamento;

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

        public String getNomePaciente() {
            return nomePaciente;
        }

        public void setNomePaciente(String nomePaciente) {
            this.nomePaciente = nomePaciente;
        }

        public String getTelefoneE164() {
            return telefoneE164;
        }

        public void setTelefoneE164(String telefoneE164) {
            this.telefoneE164 = telefoneE164;
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

        public String getObservacoes() {
            return observacoes;
        }

        public void setObservacoes(String observacoes) {
            this.observacoes = observacoes;
        }

        public UUID getIdAgendamento() {
            return idAgendamento;
        }

        public void setIdAgendamento(UUID idAgendamento) {
            this.idAgendamento = idAgendamento;
        }
    }
}
