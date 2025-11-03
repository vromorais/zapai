package br.com.zapai.atendimento.servico;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class GerenciadorSessaoRedis {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration tempoExpiracao;

    public GerenciadorSessaoRedis(StringRedisTemplate redisTemplate,
                                  ObjectMapper objectMapper,
                                  @Value("${sessao.ttl-minutos:60}") long ttlMinutos) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.tempoExpiracao = Duration.ofMinutes(ttlMinutos);
    }

    public Optional<SessaoCliente> recuperarSessao(String telefone) {
        String chave = chaveSessao(telefone);
        return Optional.ofNullable(redisTemplate.opsForValue().get(chave))
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, SessaoCliente.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                });
    }

    public void salvarSessao(SessaoCliente sessao) {
        sessao.setAtualizadoEm(OffsetDateTime.now());
        try {
            String json = objectMapper.writeValueAsString(sessao);
            redisTemplate.opsForValue().set(chaveSessao(sessao.getTelefone()), json, tempoExpiracao);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível serializar a sessão do cliente", e);
        }
    }

    public void limparSessao(String telefone) {
        redisTemplate.delete(chaveSessao(telefone));
    }

    private String chaveSessao(String telefone) {
        return "session:" + telefone;
    }
}
