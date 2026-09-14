package com.secondhand.platform.auth.jwt;

import com.secondhand.platform.common.exception.InvalidTokenException;
import com.secondhand.platform.common.exception.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        try{
             if(header !=null && header.startsWith("Bearer ")){
                String token = header.substring(7);
                jwtTokenProvider.validateToken(token);

                if(jwtTokenProvider.isAccessToken(token)) {
                    Long userId = jwtTokenProvider.getUserId(token);
                    String role = jwtTokenProvider.getRole(token);

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }else{
                    throw new InvalidTokenException("Access Token이 아닙니다.");
                }
            }
            filterChain.doFilter(request, response);
        }catch(TokenExpiredException e){
            SecurityContextHolder.clearContext();
            writeUnauthorized(
                    response,
                    "토큰이 만료되었습니다.",
                    "TOKEN_EXPIRED"
            );
        }catch (InvalidTokenException e) {
            SecurityContextHolder.clearContext();

            writeUnauthorized(
                    response,
                    "유효하지 않은 토큰입니다.",
                    "INVALID_TOKEN"
            );
        }
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message,
            String errorCode
    ) throws IOException{
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write("""
                {
                    "message": "%s",
                    "errorCode": "%s"
                }
                """.formatted(message, errorCode));
    }
}
