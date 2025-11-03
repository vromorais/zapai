package br.com.zapai.atendimento.servico;

import br.com.zapai.atendimento.dominio.ContaOauth;
import br.com.zapai.atendimento.dominio.Profissional;
import br.com.zapai.atendimento.repositorio.ContaOauthRepositorio;
import br.com.zapai.atendimento.repositorio.ProfissionalRepositorio;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ServicoOauthGoogle {

    private final String clienteId;
    private final String clienteSegredo;
    private final String redirectUri;
    private final String escopos;
    private final String baseOauth;
    private final String baseToken;
    private final WebClient webClient;
    private final ProfissionalRepositorio profissionalRepositorio;
    private final ContaOauthRepositorio contaOauthRepositorio;

    public ServicoOauthGoogle(@Value("${integracao-google.cliente-id}") String clienteId,
                              @Value("${integracao-google.cliente-segredo}") String clienteSegredo,
                              @Value("${integracao-google.redirect-uri}") String redirectUri,
                              @Value("${integracao-google.escopos}") String escopos,
                              @Value("${integracao-google.base-oauth}") String baseOauth,
                              @Value("${integracao-google.base-token}") String baseToken,
                              WebClient webClient,
                              ProfissionalRepositorio profissionalRepositorio,
                              ContaOauthRepositorio contaOauthRepositorio) {
        this.clienteId = clienteId;
        this.clienteSegredo = clienteSegredo;
        this.redirectUri = redirectUri;
        this.escopos = escopos;
        this.baseOauth = baseOauth;
        this.baseToken = baseToken;
        this.webClient = webClient;
        this.profissionalRepositorio = profissionalRepositorio;
        this.contaOauthRepositorio = contaOauthRepositorio;
        if (clienteId == null || clienteId.isBlank() || clienteSegredo == null || clienteSegredo.isBlank()) {
            throw new IllegalStateException("Credenciais do Google OAuth não configuradas");
        }
    }

    public String gerarUrlAutorizacao(UUID profissionalId) {
        return UriComponentsBuilder.fromHttpUrl(baseOauth)
                .queryParam("client_id", clienteId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", escopos)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", profissionalId.toString())
                .build()
                .toUriString();
    }

    public void processarCallback(String codigo, UUID profissionalId) {
        Profissional profissional = profissionalRepositorio.findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        JsonNode resposta = webClient.post()
                .uri(baseToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "code", codigo,
                        "client_id", clienteId,
                        "client_secret", clienteSegredo,
                        "redirect_uri", redirectUri,
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        ContaOauth conta = contaOauthRepositorio
                .findByProvedorAndProfissionalId("GOOGLE", profissional.getId())
                .orElseGet(ContaOauth::new);

        conta.setId(conta.getId() != null ? conta.getId() : UUID.randomUUID());
        conta.setProvedor("GOOGLE");
        conta.setProfissional(profissional);
        conta.setAccessToken(resposta.path("access_token").asText());
        conta.setRefreshToken(resposta.path("refresh_token").asText(null));
        conta.setEscopo(resposta.path("scope").asText());
        long expiresIn = resposta.path("expires_in").asLong(0);
        conta.setExpiraEm(OffsetDateTime.now().plusSeconds(expiresIn));

        contaOauthRepositorio.save(conta);
    }
}
