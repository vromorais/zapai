package br.com.zapai.atendimento.servico;

import br.com.zapai.atendimento.dominio.Agendamento;
import br.com.zapai.atendimento.dominio.Clinica;
import br.com.zapai.atendimento.dominio.Disponibilidade;
import br.com.zapai.atendimento.dominio.Paciente;
import br.com.zapai.atendimento.dominio.Profissional;
import br.com.zapai.atendimento.dominio.enums.StatusAgendamento;
import br.com.zapai.atendimento.dominio.enums.StatusDisponibilidade;
import br.com.zapai.atendimento.dto.PlanoAcao;
import br.com.zapai.atendimento.repositorio.AgendamentoRepositorio;
import br.com.zapai.atendimento.repositorio.ClinicaRepositorio;
import br.com.zapai.atendimento.repositorio.DisponibilidadeRepositorio;
import br.com.zapai.atendimento.repositorio.PacienteRepositorio;
import br.com.zapai.atendimento.repositorio.ProfissionalRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ServicoAgendamento {

    private final PacienteRepositorio pacienteRepositorio;
    private final ProfissionalRepositorio profissionalRepositorio;
    private final ClinicaRepositorio clinicaRepositorio;
    private final DisponibilidadeRepositorio disponibilidadeRepositorio;
    private final AgendamentoRepositorio agendamentoRepositorio;
    private final ServicoGoogleCalendar servicoGoogleCalendar;

    public ServicoAgendamento(PacienteRepositorio pacienteRepositorio,
                               ProfissionalRepositorio profissionalRepositorio,
                               ClinicaRepositorio clinicaRepositorio,
                               DisponibilidadeRepositorio disponibilidadeRepositorio,
                               AgendamentoRepositorio agendamentoRepositorio,
                               ServicoGoogleCalendar servicoGoogleCalendar) {
        this.pacienteRepositorio = pacienteRepositorio;
        this.profissionalRepositorio = profissionalRepositorio;
        this.clinicaRepositorio = clinicaRepositorio;
        this.disponibilidadeRepositorio = disponibilidadeRepositorio;
        this.agendamentoRepositorio = agendamentoRepositorio;
        this.servicoGoogleCalendar = servicoGoogleCalendar;
    }

    @Transactional
    public Agendamento criarOuAtualizarAgendamento(PlanoAcao.DadosAgendamento dados) {
        Profissional profissional = profissionalRepositorio.findById(dados.getProfissionalId())
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        Clinica clinica = clinicaRepositorio.findById(dados.getClinicaId())
                .orElseThrow(() -> new IllegalArgumentException("Clínica não encontrada"));

        Paciente paciente = recuperarOuCriarPaciente(dados);

        Disponibilidade disponibilidade = disponibilidadeRepositorio
                .findFirstByProfissionalIdAndStatusAndInicioEmLessThanEqualAndFimEmGreaterThanEqual(
                        profissional.getId(), StatusDisponibilidade.LIVRE, dados.getInicioEm(), dados.getFimEm())
                .orElseThrow(() -> new IllegalStateException("Não há disponibilidade para o horário solicitado"));

        disponibilidade.setStatus(StatusDisponibilidade.RESERVADO);
        disponibilidadeRepositorio.save(disponibilidade);

        Agendamento agendamento = dados.getIdAgendamento() != null ?
                agendamentoRepositorio.findById(dados.getIdAgendamento()).orElse(new Agendamento()) :
                new Agendamento();

        agendamento.setId(agendamento.getId() != null ? agendamento.getId() : UUID.randomUUID());
        agendamento.setClinica(clinica);
        agendamento.setProfissional(profissional);
        agendamento.setPaciente(paciente);
        agendamento.setInicioEm(dados.getInicioEm());
        agendamento.setFimEm(dados.getFimEm());
        agendamento.setStatus(StatusAgendamento.AGENDADO);
        agendamento.setObservacoes(dados.getObservacoes());

        String resumo = "Consulta com " + profissional.getNome();
        String descricao = dados.getObservacoes() != null ? dados.getObservacoes() : "Agendado via ZapAI";
        String idEvento = servicoGoogleCalendar.criarEvento(profissional, dados.getInicioEm(), dados.getFimEm(), resumo, descricao);
        agendamento.setIdEventoGoogle(idEvento);

        return agendamentoRepositorio.save(agendamento);
    }

    private Paciente recuperarOuCriarPaciente(PlanoAcao.DadosAgendamento dados) {
        Optional<Paciente> pacienteExistente = pacienteRepositorio.findByTelefoneE164(dados.getTelefoneE164());
        if (pacienteExistente.isPresent()) {
            return pacienteExistente.get();
        }
        Paciente paciente = new Paciente();
        paciente.setId(UUID.randomUUID());
        paciente.setNomeCompleto(dados.getNomePaciente());
        paciente.setTelefoneE164(dados.getTelefoneE164());
        return pacienteRepositorio.save(paciente);
    }
}
