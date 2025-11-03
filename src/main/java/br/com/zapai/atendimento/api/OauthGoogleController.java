package br.com.zapai.atendimento.api;

import br.com.zapai.atendimento.servico.ServicoOauthGoogle;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/oauth/google")
public class OauthGoogleController {

    private final ServicoOauthGoogle servicoOauthGoogle;

    public OauthGoogleController(ServicoOauthGoogle servicoOauthGoogle) {
        this.servicoOauthGoogle = servicoOauthGoogle;
    }

    @GetMapping("/authorize")
    public RedirectView autorizar(@RequestParam("state") UUID profissionalId) {
        String url = servicoOauthGoogle.gerarUrlAutorizacao(profissionalId);
        return new RedirectView(url);
    }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestParam("code") String codigo,
                                                        @RequestParam("state") UUID profissionalId) {
        servicoOauthGoogle.processarCallback(codigo, profissionalId);
        return ResponseEntity.ok(Map.of("status", "conectado"));
    }
}
