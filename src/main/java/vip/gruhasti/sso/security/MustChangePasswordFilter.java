package vip.gruhasti.sso.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Backend enforcement of the "must set a permanent password before doing anything else" rule.
 * A token minted for a user still on their temp password carries mustChangePassword=true; any
 * request bearing such a token is rejected unless it targets an allow-listed path.
 *
 * /api/auth/** MUST stay on the allow-list — that's the prefix /api/auth/activate-password lives
 * under, and it's the one endpoint that clears the flag. Blocking it here would permanently lock
 * out any user mid-activation, since the frontend has no other token to attach to that call.
 */
@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isValid(token)) {
                Claims claims = jwtUtil.parse(token);
                Boolean mustChangePassword = claims.get("mustChangePassword", Boolean.class);
                if (Boolean.TRUE.equals(mustChangePassword)) {
                    String path = request.getRequestURI();
                    boolean allowed = path.startsWith("/api/auth/") || path.startsWith("/api/email-templates/");
                    if (!allowed) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Set your permanent password before continuing\"}");
                        return;
                    }
                }
            }
        }
        chain.doFilter(request, response);
    }
}
