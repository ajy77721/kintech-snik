package com.kitchen.sink.service;


import com.kitchen.sink.dto.LoginRequestDTO;
import com.kitchen.sink.dto.LoginResponseDTO;
import com.kitchen.sink.dto.LogoutResponseDTO;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.repo.UserSessionRepository;
import com.kitchen.sink.service.impl.AuthServiceImpl;
import com.kitchen.sink.utils.JWTUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private AuthServiceImpl authService;
    private LoginRequestDTO loginRequest;
    private Authentication authentication;
    private UserSession userSession;
    private UserDTO userDTO;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginRequest = new LoginRequestDTO("test@example.com", "password");
        userDTO = new UserDTO("1", "Test User", "test@example.com", "password", Set.of(), UserStatus.ACTIVE);
        userSession = new UserSession("1", "test@example.com", "validToken", LocalDateTime.now());
        authentication = new UsernamePasswordAuthenticationToken("test@example.com", "password");


    }

    @Test
    void testLogin_Successful() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userSession));
        when(jwtUtils.validateToken("validToken", "test@example.com")).thenReturn(true);
        when(jwtUtils.validateRoles("validToken", authentication.getAuthorities())).thenReturn(true);

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("validToken", response.token());
    }

    @Test
    void testLogin_InvalidCredentials() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_ExistingSessionWithInvalidToken() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userSession));
        when(jwtUtils.validateToken(any(), any())).thenReturn(false);
        when(jwtUtils.generateToken("test@example.com", userDTO.roles())).thenReturn("newToken");

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("newToken", response.token());
    }

    @Test
    void testLogin_NoExistingSession() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        String newToken = "newToken";
        when(jwtUtils.generateToken("test@example.com", userDTO.roles())).thenReturn(newToken);

        LoginResponseDTO response = authService.login(loginRequest);

        assertEquals(newToken, response.token());
        assertEquals("test@example.com", response.email());
        verify(userSessionRepository, never()).deleteById(anyString());
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testLogin_ExistingSessionWithTokenValidationException() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userSession));
        when(jwtUtils.validateToken(anyString(), anyString())).thenThrow(new RuntimeException("Token validation error"));

        String newToken = "newToken";
        when(jwtUtils.generateToken("test@example.com", userDTO.roles())).thenReturn(newToken);

        LoginResponseDTO response = authService.login(loginRequest);

        assertEquals(newToken, response.token());
        assertEquals("test@example.com", response.email());
        verify(userSessionRepository).deleteById(userSession.getId());
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testLogin_ExistingSessionWithRoleValidationException() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userSession));
        when(jwtUtils.validateToken(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.validateRoles(anyString(), anyCollection())).thenThrow(new RuntimeException("Role validation error"));

        String newToken = "newToken";
        when(jwtUtils.generateToken("test@example.com", userDTO.roles())).thenReturn(newToken);

        LoginResponseDTO response = authService.login(loginRequest);

        assertEquals(newToken, response.token());
        assertEquals("test@example.com", response.email());
        verify(userSessionRepository).deleteById(userSession.getId());
        verify(userSessionRepository).save(any(UserSession.class));
    }

    @Test
    void testLogout_Successful() {
        String token = "Bearer validToken";

        doNothing().when(userSessionRepository).deleteByTokenAndEmail("validToken",anyString());

        LogoutResponseDTO response = authService.logout(token);

        assertEquals("Logout successfully", response.message());
        verify(userSessionRepository, times(1)).deleteByTokenAndEmail("validToken",anyString());
    }

    @Test
    void testLogout_InvalidToken() {
        String token = "InvalidToken";

        LogoutResponseDTO response = authService.logout(token);

        assertEquals("Logout successfully", response.message());
        verify(userSessionRepository, never()).deleteByTokenAndEmail(anyString(),anyString());
    }

    @Test
    void testLogout_TokenIsNull() {
        String token = null;

        LogoutResponseDTO response = authService.logout(token);

        assertEquals("Logout successfully", response.message());
        verify(userSessionRepository, never()).deleteByTokenAndEmail(anyString(),anyString());
    }

}