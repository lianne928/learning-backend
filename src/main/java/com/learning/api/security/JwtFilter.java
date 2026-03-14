package com.learning.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (!jwtService.isTokenExp(token)) {
                    Long userId = jwtService.extractUserId(token);
                    Integer role  = jwtService.extractUserRole(token);

                    String roleName = switch (role) {
                        case 1 -> "ROLE_STUDENT";
                        case 2 -> "ROLE_TEACHER";
                        case 3 -> "ROLE_ADMIN";
                        default -> null;
                    };

                    List<GrantedAuthority> authorities = roleName != null
                            ? List.of(new SimpleGrantedAuthority(roleName))
                            : List.of();

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                // token 格式錯誤或簽名不符 — 不設定 auth，後續由 Spring Security 決定是否拒絕
            }
        }

        filterChain.doFilter(request, response);
    }
}
