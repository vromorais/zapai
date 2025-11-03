package br.com.zapai.atendimento.servico;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class ServicoWhatsapp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServicoWhatsapp.class);

    private final WebClient webClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String token;
    private final String baseUrl;
    private final String idNumero;

    public ServicoWhatsapp(WebClient webClient,
                           StringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper,
                           @Value("${whatsapp.token-env:WHATSAPP_TOKEN}") String variavelToken,
                           @Value("${whatsapp.token:}") String tokenConfigurado,
                           @Value("${whatsapp.base-url}") String baseUrl,
                           @Value("${whatsapp.id-numero}") String idNumero) {
        this.webClient = webClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        String tokenEnv = System.getenv(variavelToken);
        this.token = tokenEnv != null && !tokenEnv.isBlank() ? tokenEnv : tokenConfigurado;
        this.baseUrl = baseUrl;
        this.idNumero = idNumero;
        if (this.token == null || this.token.isBlank()) {
            throw new IllegalStateException("Token do WhatsApp não configurado");
        }
        if (this.idNumero == null || this.idNumero.isBlank()) {
            throw new IllegalStateException("Identificador do número do WhatsApp não configurado");
        }
    }

    public void enfileirarTexto(String telefoneE164, String mensagem) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", telefoneE164,
                "type", "text",
                "text", Map.of("preview_url", false, "body", mensagem)
        );
        String json = escreverJson(payload);
        redisTemplate.opsForList().leftPush("outbox:whatsapp", json);
        processarFila();
    }

    public void processarFila() {
        String item;
        while ((item = redisTemplate.opsForList().rightPop("outbox:whatsapp")) != null) {
            enviarParaWhatsapp(item)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(30)))
                    .onErrorResume(ex -> {
                        LOGGER.error("Falha ao enviar mensagem para WhatsApp, reenfileirando", ex);
                        redisTemplate.opsForList().leftPush("outbox:whatsapp", item);
                        return Mono.empty();
                    })
                    .block();
        }
    }

    private Mono<Void> enviarParaWhatsapp(String corpoJson) {
        if (token == null || token.isBlank()) {
            return Mono.error(new IllegalStateException("Token do WhatsApp não configurado"));
        }
        String url = String.format("%s/%s/messages", baseUrl, idNumero);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(token))
                .bodyValue(corpoJson)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> LOGGER.debug("Mensagem enviada para WhatsApp"));
    }

    private String escreverJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar payload do WhatsApp", e);
        }
    }
}
