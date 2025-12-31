package br.com.newcred.adapters.config.security;


import br.com.newcred.adapters.dto.OperadorPrincipalDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = auth.substring("Bearer ".length()).trim();

        try {
            OperadorPrincipalDto principal = jwtService.validarEExtrair(token);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()));
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            // token inválido -> limpa e segue (ou retorna 401, sua escolha)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}