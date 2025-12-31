package br.com.newcred.adapters.config.security;

import br.com.newcred.adapters.dto.OperadorPrincipalDto;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class OperadorContext {
    private OperadorContext() {}

    public static long getOperadorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Não autenticado");

        Object principal = auth.getPrincipal();
        if (principal instanceof OperadorPrincipalDto op) return op.operadorId();

        throw new IllegalStateException("Principal inválido");
    }

    public static String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (principal instanceof OperadorPrincipalDto op) return op.role();
        return null;
    }
}