package com.kitchen.sink.service.impl;

import com.kitchen.sink.dto.*;
import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.repo.UserSessionRepository;
import com.kitchen.sink.service.AuthService;
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionRepository userSessionRepository;
    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("invalid user request !");
        }
        UserDTO user = userService.getUserDTOByEmail(loginRequest.email());
        UserSession byUsername = userSessionRepository.findByUsername(user.email());
        if (byUsername != null) {
            if (jwtUtil.validateToken(byUsername.getToken(), user.email())) {
                if (jwtUtil.validateRoles(byUsername.getToken(), authentication.getAuthorities())) {
                return LoginResponseDTO.builder()
                        .token(byUsername.getToken())
                        .email(user.email()).build();
                }
            }
            userSessionRepository.deleteById(byUsername.getId());
        }

        String jwt = jwtUtil.generateToken(user.email(), user.roles());
        userSessionRepository.save(UserSession.builder().username(user.email()).token(jwt).build());
        return LoginResponseDTO.builder()
                .token(jwt)
                .email(user.email())
                .build();

    }

    @Override
    public LogoutResponseDTO logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            userSessionRepository.deleteByToken(jwt);
        }
        return LogoutResponseDTO.builder().message("Logout success").build();

    }
}
