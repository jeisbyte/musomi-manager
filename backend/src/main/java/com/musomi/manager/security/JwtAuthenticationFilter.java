package com.musomi.manager.security;

import com.musomi.manager.exception.AuthenticationException;
import com.musomi.manager.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "/api/v1/auth/login".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    throw new AuthenticationException(ErrorCode.SESSION_EXPIRED);
                }

                Long userId = jwtTokenProvider.getUserId(token);
                Long schoolId = jwtTokenProvider.getSchoolId(token);
                String role = jwtTokenProvider.getRole(token);

                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(schoolId);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (AuthenticationException e) {
                // Filter runs before DispatcherServlet, so @RestControllerAdvice
                // cannot catch this directly. Delegate to the resolver so the
                // GlobalExceptionHandler returns the standard error envelope.
                SecurityContextHolder.clearContext();
                handlerExceptionResolver.resolveException(request, response, null, e);
                return;
            } catch (Exception e) {
                log.debug("Unexpected error while processing JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                handlerExceptionResolver.resolveException(request, response, null,
                        new AuthenticationException(ErrorCode.SESSION_EXPIRED));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}