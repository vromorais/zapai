package br.com.zapai.atendimento.config;

import java.util.Optional;
import java.util.UUID;

public final class ContextoTenant {

    private static final ThreadLocal<UUID> TENANT_ATUAL = new ThreadLocal<>();

    private ContextoTenant() {
    }

    public static void definirTenant(UUID tenantId) {
        TENANT_ATUAL.set(tenantId);
    }

    public static Optional<UUID> obterTenant() {
        return Optional.ofNullable(TENANT_ATUAL.get());
    }

    public static void limpar() {
        TENANT_ATUAL.remove();
    }
}
