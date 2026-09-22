package com.kineplan.shared.infrastructure.security;

import com.kineplan.shared.infrastructure.tenancy.TenantContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            SecurityContextHolder.clearContext();
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtService.parse(header.substring(7));
                if ("cabinet".equals(claims.get("token_type", String.class))) {
                    UUID cabinetId = UUID.fromString(claims.get("cabinet_id", String.class));
                    UUID userId = UUID.fromString(claims.getSubject());
                    UUID membershipId = UUID.fromString(claims.get("membership_id", String.class));
                    String role = claims.get("role", String.class);
                    if (role == null || role.isBlank()) {
                        throw new IllegalArgumentException("Missing role claim");
                    }
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                    authentication.setDetails(claims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    TenantContext.set(cabinetId);
                    request.setAttribute("kineplan.membership_id", membershipId);
                } else if ("pre-auth".equals(claims.get("token_type", String.class))) {
                    UUID userId = UUID.fromString(claims.getSubject());
                    var authentication = new UsernamePasswordAuthenticationToken(
                            userId, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_PRE_AUTH")));
                    authentication.setDetails(claims);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } finally {
            TenantContext.clear();
        }
    }
}