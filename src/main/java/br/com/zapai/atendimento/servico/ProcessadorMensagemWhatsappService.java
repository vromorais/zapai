package br.com.zapai.atendimento.servico;

import br.com.zapai.atendimento.config.ContextoTenant;
import br.com.zapai.atendimento.dominio.Agendamento;
import br.com.zapai.atendimento.dominio.LogMensagem;
import br.com.zapai.atendimento.dominio.enums.StatusAgendamento;
import br.com.zapai.atendimento.dto.MensagemWhatsapp;
import br.com.zapai.atendimento.dto.PlanoAcao;
import br.com.zapai.atendimento.dto.TipoPlanoAcao;
import br.com.zapai.atendimento.repositorio.AgendamentoRepositorio;
import br.com.zapai.atendimento.repositorio.LogMensagemRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProcessadorMensagemWhatsappService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessadorMensagemWhatsappService.class);

    private final LogMensagemRepositorio logMensagemRepositorio;
    private final AgendamentoRepositorio agendamentoRepositorio;
    private final GerenciadorSessaoRedis gerenciadorSessaoRedis;
    private final ClienteOpenAiService clienteOpenAiService;
    private final ServicoAgendamento servicoAgendamento;
    private final ServicoWhatsapp servicoWhatsapp;

    public ProcessadorMensagemWhatsappService(LogMensagemRepositorio logMensagemRepositorio,
                                              AgendamentoRepositorio agendamentoRepositorio,
                                              GerenciadorSessaoRedis gerenciadorSessaoRedis,
                                              ClienteOpenAiService clienteOpenAiService,
                                              ServicoAgendamento servicoAgendamento,
                                              ServicoWhatsapp servicoWhatsapp) {
        this.logMensagemRepositorio = logMensagemRepositorio;
        this.agendamentoRepositorio = agendamentoRepositorio;
        this.gerenciadorSessaoRedis = gerenciadorSessaoRedis;
        this.clienteOpenAiService = clienteOpenAiService;
        this.servicoAgendamento = servicoAgendamento;
        this.servicoWhatsapp = servicoWhatsapp;
    }

    @Transactional
    public void processarMensagem(MensagemWhatsapp mensagem) {
        registrarLog(mensagem, "IN", null);
        SessaoCliente sessao = gerenciadorSessaoRedis.recuperarSessao(mensagem.getTelefone())
                .orElseGet(() -> criarSessao(mensagem));
        sessao.setTelefone(mensagem.getTelefone());

        sessao.getContexto().put("ultima_mensagem_recebida", mensagem.getMensagem());

        if (ContextoTenant.obterTenant().isEmpty() && sessao.getClinicaId() != null) {
            ContextoTenant.definirTenant(sessao.getClinicaId());
        }

        if (mensagem.getMensagem() == null || mensagem.getMensagem().isBlank()) {
            String respostaPadrao = "Recebemos sua mensagem, poderia enviar o texto novamente?";
            servicoWhatsapp.enfileirarTexto(mensagem.getTelefone(), respostaPadrao);
            registrarLog(mensagem, "OUT", respostaPadrao);
            return;
        }

        PlanoAcao plano = clienteOpenAiService.obterPlanoAcao(mensagem.getMensagem(), sessao);

        String resposta = executarPlano(plano, sessao);

        gerenciadorSessaoRedis.salvarSessao(sessao);

        servicoWhatsapp.enfileirarTexto(mensagem.getTelefone(), resposta);
        registrarLog(mensagem, "OUT", resposta);
        LOGGER.info("Plano de ação aplicado com sucesso para telefone {}", mensagem.getTelefone());
    }

    private SessaoCliente criarSessao(MensagemWhatsapp mensagem) {
        SessaoCliente sessao = new SessaoCliente();
        sessao.setTelefone(mensagem.getTelefone());
        return sessao;
    }

    private void registrarLog(MensagemWhatsapp mensagem, String direcao, String resposta) {
        LogMensagem log = new LogMensagem();
        log.setId(UUID.randomUUID());
        log.setCanal("WHATSAPP");
        log.setDirecao(direcao);
        log.setTelefoneE164(mensagem.getTelefone());
        log.setPayload(String.valueOf(mensagem.getDadosOriginais()));
        log.setCriadoEm(OffsetDateTime.now());
        log.setIntent("PLANO_ACAO");
        log.setIdSessao(mensagem.getTelefone());
        log.setIdCorrelacao(mensagem.getIdMensagem());
        if (resposta != null) {
            log.setPayload(resposta);
        }
        logMensagemRepositorio.save(log);
    }

    private String executarPlano(PlanoAcao plano, SessaoCliente sessao) {
        if (plano == null || plano.getTipo() == null) {
            return "Não consegui entender sua solicitação. Pode reformular, por favor?";
        }
        sessao.getContexto().put("ultimo_plano", plano);
        TipoPlanoAcao tipo = plano.getTipo();
        return switch (tipo) {
            case FAQ, SMALL_TALK -> plano.getRespostaUsuario();
            case BOOK -> tratarAgendamento(plano, sessao);
            case RESCHEDULE -> tratarReagendamento(plano, sessao);
            case CANCEL -> tratarCancelamento(plano, sessao);
        };
    }

    private String tratarAgendamento(PlanoAcao plano, SessaoCliente sessao) {
        List<String> pendencias = plano.getPerguntasPendentes();
        if (pendencias != null && !pendencias.isEmpty()) {
            return "Preciso de mais informações: " + String.join(", ", pendencias);
        }
        PlanoAcao.DadosAgendamento dados = plano.getDadosAgendamento();
        if (dados == null) {
            return "Não encontrei dados suficientes para agendar. Pode informar data, horário e profissional?";
        }
        Agendamento agendamento = servicoAgendamento.criarOuAtualizarAgendamento(dados);
        sessao.setClinicaId(agendamento.getClinica().getId());
        sessao.setProfissionalId(agendamento.getProfissional().getId());
        sessao.setPacienteId(agendamento.getPaciente().getId());
        return String.format("Agendamento confirmado para %s às %s. Enviaremos detalhes por WhatsApp!",
                agendamento.getInicioEm().toLocalDate(), agendamento.getInicioEm().toLocalTime());
    }

    private String tratarReagendamento(PlanoAcao plano, SessaoCliente sessao) {
        PlanoAcao.DadosAgendamento dados = plano.getDadosAgendamento();
        if (dados == null || dados.getIdAgendamento() == null) {
            return "Para reagendar preciso do identificador do compromisso anterior.";
        }
        Agendamento existente = agendamentoRepositorio.findById(dados.getIdAgendamento())
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        existente.setStatus(StatusAgendamento.REAGENDADO);
        agendamentoRepositorio.save(existente);
        plano.getDadosAgendamento().setPacienteId(existente.getPaciente().getId());
        Agendamento novoAgendamento = servicoAgendamento.criarOuAtualizarAgendamento(plano.getDadosAgendamento());
        sessao.setPacienteId(novoAgendamento.getPaciente().getId());
        sessao.setProfissionalId(novoAgendamento.getProfissional().getId());
        sessao.setClinicaId(novoAgendamento.getClinica().getId());
        return "Seu agendamento foi remarcado com sucesso.";
    }

    private String tratarCancelamento(PlanoAcao plano, SessaoCliente sessao) {
        PlanoAcao.DadosAgendamento dados = plano.getDadosAgendamento();
        if (dados == null || dados.getIdAgendamento() == null) {
            return "Informe o agendamento que deseja cancelar.";
        }
        Agendamento agendamento = agendamentoRepositorio.findById(dados.getIdAgendamento())
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        agendamento.setStatus(StatusAgendamento.CANCELADO);
        agendamentoRepositorio.save(agendamento);
        return "Seu agendamento foi cancelado. Se precisar de um novo horário, é só me avisar!";
    }
}
