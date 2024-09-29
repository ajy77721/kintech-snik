package com.kitchen.sink.filter;

import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.exception.UserRolesModifiedException;
import com.kitchen.sink.repo.UserSessionRepository;
import com.kitchen.sink.service.impl.UserDetailsServiceImpl;
import com.kitchen.sink.utils.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.kitchen.sink.constants.JWTConstant.AUTHORIZATION;
import static com.kitchen.sink.constants.JWTConstant.BEARER;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils JWTUtils;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER)) {
            jwt = authorizationHeader.substring(7);
            username = JWTUtils.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (JWTUtils.validateToken(jwt, userDetails.getUsername())) {
               Optional<UserSession> userSession= userSessionRepository.findByEmail(username);
               if( userSession.isEmpty() || !userSession.get().getToken().equals(jwt)){
                   throw new InsufficientAuthenticationException("Token is not valid or expired");
               }
               if (!JWTUtils.validateRoles(jwt, userDetails.getAuthorities())){
                   throw new UserRolesModifiedException("User roles Modified, Please login again");
               }
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

            }
        }
        chain.doFilter(request, response);
    }
}

