package br.com.zapai.atendimento.servico;

import br.com.zapai.atendimento.dto.PlanoAcao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ClienteOpenAiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteOpenAiService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String modelo;
    private final String chaveApi;
    private final String baseUrl;

    public ClienteOpenAiService(WebClient webClient,
                                ObjectMapper objectMapper,
                                @Value("${openai.modelo}") String modelo,
                                @Value("${openai.base-url}") String baseUrl,
                                @Value("${openai.chave-env:OPENAI_API_KEY}") String variavelAmbienteChave,
                                @Value("${openai.chave:}") String chaveConfigurada) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.modelo = modelo;
        this.baseUrl = baseUrl;
        String chave = System.getenv(variavelAmbienteChave);
        this.chaveApi = (chave != null && !chave.isBlank()) ? chave : chaveConfigurada;
        if (this.chaveApi == null || this.chaveApi.isBlank()) {
            throw new IllegalStateException("Chave da OpenAI não configurada");
        }
    }

    public PlanoAcao obterPlanoAcao(String mensagemCliente, SessaoCliente sessaoAtual) {
        String promptSistema = "Você é um assistente de agendamento de clínicas médicas. Responda sempre em JSON.";
        JsonNode resposta = webClient.post()
                .uri(baseUrl + "/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(chaveApi))
                .bodyValue(Map.of(
                        "model", modelo,
                        "response_format", Map.of("type", "json_schema", "json_schema", Map.of(
                                "name", "plano_acao",
                                "schema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "tipo", Map.of("type", "string"),
                                                "resposta_usuario", Map.of("type", "string"),
                                                "perguntas_pendentes", Map.of("type", "array", "items", Map.of("type", "string")),
                                                "dados_agendamento", Map.of("type", "object")
                                        ),
                                        "required", List.of("tipo", "resposta_usuario")
                                )
                        )),
                        "messages", List.of(
                                Map.of("role", "system", "content", promptSistema),
                                Map.of("role", "user", "content", montarPromptUsuario(mensagemCliente, sessaoAtual))
                        )
                ))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .doOnError(erro -> LOGGER.error("Erro ao consultar OpenAI", erro))
                .onErrorResume(erro -> Mono.empty())
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Sem resposta da OpenAI"));

        JsonNode conteudo = resposta.path("choices").path(0).path("message").path("content");
        try {
            return objectMapper.readValue(conteudo.asText(), PlanoAcao.class);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível interpretar o plano de ação", e);
        }
    }

    private String montarPromptUsuario(String mensagemCliente, SessaoCliente sessaoAtual) {
        StringBuilder builder = new StringBuilder();
        builder.append("Mensagem do cliente: ").append(mensagemCliente).append("\n");
        if (sessaoAtual != null && sessaoAtual.getContexto() != null) {
            builder.append("Contexto atual: ").append(sessaoAtual.getContexto()).append("\n");
        }
        builder.append("Responda com o plano de ação em JSON seguindo o esquema informado.");
        return builder.toString();
    }
}
