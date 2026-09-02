package vip.gruhasti.sso.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Authenticates backend-to-backend calls (e.g. from shuddha-api / pen2paper-api) that carry a
 * shared secret in the X-Internal-Api-Key header, instead of a user JWT. There is no logged-in
 * user making these calls, so this grants a synthetic ROLE_INTERNAL_SERVICE authority rather than
 * resolving a User from the database.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String internalApiKey;

    public InternalApiKeyFilter(@Value("${gruhasti.internal.api-key}") String internalApiKey) {
        this.internalApiKey = internalApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("X-Internal-Api-Key");
        if (StringUtils.hasText(header) && MessageDigest.isEqual(
                header.getBytes(StandardCharsets.UTF_8), internalApiKey.getBytes(StandardCharsets.UTF_8))) {
            var auth = new UsernamePasswordAuthenticationToken(
                    "internal-service", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
