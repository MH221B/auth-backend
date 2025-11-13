package com.example.auth_backend.config;

import com.example.auth_backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtService.validateAccessToken(token)) {
                    Map<String, Object> claims = jwtService.parseClaims(token);
                    Object usernameObj = claims.get("username");
                    Object sub = claims.get("sub");

                    String principalName = usernameObj != null ? usernameObj.toString() : (sub != null ? sub.toString() : null);

                    List<GrantedAuthority> authorities = new ArrayList<>();
                    Object rolesVal = claims.get("roles");
                    if (rolesVal instanceof List) {
                        for (Object r : (List<?>) rolesVal) {
                            if (r != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
                        }
                    }

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principalName, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
