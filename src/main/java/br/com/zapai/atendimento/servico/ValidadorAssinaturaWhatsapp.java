package br.com.zapai.atendimento.servico;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class ValidadorAssinaturaWhatsapp {

    private static final String PREFIXO_ASSINATURA = "sha256=";

    @Value("${whatsapp.segredo-assinatura-env:WHATSAPP_SECRET}")
    private String variavelAmbienteSegredo;

    @Value("${whatsapp.segredo-assinatura:}")
    private String segredoConfigurado;

    private byte[] segredo;

    @PostConstruct
    public void carregarSegredo() {
        String valor = System.getenv(variavelAmbienteSegredo);
        if (valor == null || valor.isBlank()) {
            valor = segredoConfigurado;
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Segredo de assinatura do WhatsApp não configurado");
        }
        this.segredo = valor.getBytes(StandardCharsets.UTF_8);
    }

    public boolean assinaturaValida(String assinaturaCabecalho, String corpo) {
        if (assinaturaCabecalho == null || corpo == null) {
            return false;
        }
        if (!assinaturaCabecalho.startsWith(PREFIXO_ASSINATURA)) {
            return false;
        }
        String assinaturaInformada = assinaturaCabecalho.substring(PREFIXO_ASSINATURA.length()).toLowerCase();
        String assinaturaCalculada = calcularAssinatura(corpo).toLowerCase();
        return slowEquals(assinaturaInformada, assinaturaCalculada);
    }

    private String calcularAssinatura(String corpo) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            byte[] hash = hmac.doFinal(corpo.getBytes(StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        for (byte b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao calcular assinatura do WhatsApp", e);
        }
    }

    private boolean slowEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
