package br.com.zapai.atendimento.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class FiltroTenant extends OncePerRequestFilter {

    public static final String CABECALHO_TENANT = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            Optional.ofNullable(request.getHeader(CABECALHO_TENANT))
                    .map(this::parseTenant)
                    .ifPresent(ContextoTenant::definirTenant);

            ContextoTenant.obterTenant()
                    .map(UUID::toString)
                    .ifPresent(valor -> MDC.put("tenant", valor));

            filterChain.doFilter(request, response);
        } finally {
            ContextoTenant.limpar();
            MDC.remove("tenant");
        }
    }

    private UUID parseTenant(String valor) {
        try {
            return UUID.fromString(valor);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
