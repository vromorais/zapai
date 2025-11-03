package br.com.zapai.atendimento.servico;

import br.com.zapai.atendimento.dominio.ContaOauth;
import br.com.zapai.atendimento.dominio.Profissional;
import br.com.zapai.atendimento.repositorio.ContaOauthRepositorio;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class ServicoGoogleCalendar {

    private final WebClient webClient;
    private final ContaOauthRepositorio contaOauthRepositorio;
    private final String baseCalendar;

    public ServicoGoogleCalendar(WebClient webClient,
                                 ContaOauthRepositorio contaOauthRepositorio,
                                 @Value("${integracao-google.base-calendar}") String baseCalendar) {
        this.webClient = webClient;
        this.contaOauthRepositorio = contaOauthRepositorio;
        this.baseCalendar = baseCalendar;
    }

    public String criarEvento(Profissional profissional, OffsetDateTime inicio, OffsetDateTime fim, String resumo, String descricao) {
        ContaOauth conta = contaOauthRepositorio
                .findByProvedorAndProfissionalId("GOOGLE", profissional.getId())
                .orElseThrow(() -> new IllegalStateException("Profissional sem conta Google conectada"));

        JsonNode resposta = webClient.post()
                .uri(String.format("%s/calendars/%s/events", baseCalendar, profissional.getCalendarioId()))
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(conta.getAccessToken()))
                .bodyValue(Map.of(
                        "summary", resumo,
                        "description", descricao,
                        "start", Map.of("dateTime", inicio.toString()),
                        "end", Map.of("dateTime", fim.toString())
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        return resposta.path("id").asText();
    }
}
