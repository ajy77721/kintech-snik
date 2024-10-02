package com.kitchen.sink.service.impl;

import com.kitchen.sink.dto.LoginRequestDTO;
import com.kitchen.sink.dto.LoginResponseDTO;
import com.kitchen.sink.dto.LogoutResponseDTO;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.repo.UserSessionRepository;
import com.kitchen.sink.service.AuthService;
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.kitchen.sink.constants.JWTConstant.BEARER;
import static com.kitchen.sink.constants.JWTConstant.LOGOUT_SUCCESS_MESSAGE;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Attempting to authenticate user with email: {}", loginRequest.email());
        Authentication authentication = authenticateUser(loginRequest);
        UserDTO user = userService.getUserDTOByEmail(loginRequest.email());
        Optional<UserSession> existingSession = userSessionRepository.findByEmail(user.email());

        if (existingSession.isPresent()) {
            UserSession userSession = existingSession.get();
            if (isValidSession(userSession, user, authentication)) {
                log.info("Existing valid session found for user: {}", user.email());
                return buildLoginResponse(userSession.getToken(), user.email());
            } else {
                log.warn("Invalid session found for user: {}, deleting session", user.email());
                userSessionRepository.deleteById(userSession.getId());
            }
        }

        String jwt = jwtUtils.generateToken(user.email(), user.roles());
        userSessionRepository.save(UserSession.builder().email(user.email()).token(jwt).build());
        log.info("New session created for user: {}", user.email());
        return buildLoginResponse(jwt, user.email());
    }

    @Override
    public LogoutResponseDTO logout(String token) {
        log.info("Attempting to logout user with token: {}", token);
        if (token != null && token.startsWith(BEARER)) {
            String jwt = token.substring(7);
            String email = jwtUtils.getEmail();
            userSessionRepository.deleteByTokenAndEmail(jwt,email);
            log.info("User logged out successfully");
        } else {
            log.warn("Invalid token provided for logout");
        }
        return LogoutResponseDTO.builder().message(LOGOUT_SUCCESS_MESSAGE).build();
    }

    private Authentication authenticateUser(LoginRequestDTO loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
    }

    private boolean isValidSession(UserSession userSession, UserDTO user, Authentication authentication) {
        try {
            return jwtUtils.validateToken(userSession.getToken(), user.email()) &&
                    jwtUtils.validateRoles(userSession.getToken(), authentication.getAuthorities());
        } catch (Exception e) {
            log.error("Error while validating token for user: {}", user.email(), e);
            return false;
        }
    }

    private LoginResponseDTO buildLoginResponse(String token, String email) {
        return LoginResponseDTO.builder()
                .token(token)
                .email(email)
                .build();
    }
}