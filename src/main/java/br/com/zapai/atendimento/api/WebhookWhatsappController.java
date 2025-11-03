package br.com.zapai.atendimento.api;

import br.com.zapai.atendimento.dto.MensagemWhatsapp;
import br.com.zapai.atendimento.servico.ProcessadorMensagemWhatsappService;
import br.com.zapai.atendimento.servico.ValidadorAssinaturaWhatsapp;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhooks/whatsapp")
public class WebhookWhatsappController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookWhatsappController.class);

    private final ObjectMapper objectMapper;
    private final ValidadorAssinaturaWhatsapp validadorAssinaturaWhatsapp;
    private final ProcessadorMensagemWhatsappService processadorMensagemWhatsappService;

    public WebhookWhatsappController(ObjectMapper objectMapper,
                                     ValidadorAssinaturaWhatsapp validadorAssinaturaWhatsapp,
                                     ProcessadorMensagemWhatsappService processadorMensagemWhatsappService) {
        this.objectMapper = objectMapper;
        this.validadorAssinaturaWhatsapp = validadorAssinaturaWhatsapp;
        this.processadorMensagemWhatsappService = processadorMensagemWhatsappService;
    }

    @PostMapping
    public ResponseEntity<?> receberMensagem(@RequestBody String corpo,
                                             @RequestHeader(name = "X-Hub-Signature-256", required = false) String assinatura) throws Exception {
        if (!validadorAssinaturaWhatsapp.assinaturaValida(assinatura, corpo)) {
            LOGGER.warn("Assinatura inválida recebida do WhatsApp");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Map<String, Object> payload = objectMapper.readValue(corpo, new TypeReference<Map<String, Object>>() {
        });
        MensagemWhatsapp mensagem = extrairMensagem(payload);
        if (mensagem == null) {
            LOGGER.warn("Payload do WhatsApp sem mensagem processável");
            return ResponseEntity.ok().build();
        }

        processadorMensagemWhatsappService.processarMensagem(mensagem);
        return ResponseEntity.ok(Map.of("status", "recebido"));
    }

    @SuppressWarnings("unchecked")
    private MensagemWhatsapp extrairMensagem(Map<String, Object> payload) {
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");
            if (entries == null || entries.isEmpty()) {
                return null;
            }
            Map<String, Object> entry = entries.get(0);
            List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");
            if (changes == null || changes.isEmpty()) {
                return null;
            }
            Map<String, Object> change = changes.get(0);
            Map<String, Object> value = (Map<String, Object>) change.get("value");
            List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
            if (messages == null || messages.isEmpty()) {
                return null;
            }
            Map<String, Object> message = messages.get(0);
            String telefone = (String) message.get("from");
            Map<String, Object> metadata = (Map<String, Object>) value.get("metadata");
            if (telefone == null && metadata != null) {
                telefone = (String) metadata.get("display_phone_number");
            }
            Map<String, Object> text = (Map<String, Object>) message.get("text");
            String conteudo = text != null ? (String) text.get("body") : null;
            String idMensagem = (String) message.get("id");
            return new MensagemWhatsapp(telefone, conteudo, idMensagem, payload);
        } catch (Exception e) {
            LOGGER.error("Erro ao extrair mensagem do payload do WhatsApp", e);
            return null;
        }
    }
}
