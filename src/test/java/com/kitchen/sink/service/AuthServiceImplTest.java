package com.kitchen.sink.service;


import com.kitchen.sink.dto.LoginRequestDTO;

import com.kitchen.sink.dto.LoginResponseDTO;
import com.kitchen.sink.dto.LogoutResponseDTO;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.enums.UserRole;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Successful() {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDTO userDTO = new UserDTO("1", "test", "test@example.com", "passowrd",Set.of(UserRole.VISITOR), null);
        UserSession userSession = new UserSession("1", "test@example.com", "validToken", null);

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
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDTO userDTO = new UserDTO("1", "test", "test@example.com", "passowrd",Set.of(UserRole.VISITOR), null);
        UserSession userSession = new UserSession("1", "test@example.com", "invalidToken", null);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userService.getUserDTOByEmail("test@example.com")).thenReturn(userDTO);
        when(userSessionRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userSession));
        when(jwtUtils.validateToken("invalidToken", "test@example.com")).thenReturn(false);
        when(jwtUtils.generateToken("test@example.com", userDTO.roles())).thenReturn("newToken");

        LoginResponseDTO response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.email());
        assertEquals("newToken", response.token());
    }
    @Test
    void testLogout_Successful() {
        String token = "Bearer validToken";

        doNothing().when(userSessionRepository).deleteByToken("validToken");

        LogoutResponseDTO response = authService.logout(token);

        assertEquals("Logout successfully", response.message());
        verify(userSessionRepository, times(1)).deleteByToken("validToken");
    }
    @Test
    void testLogout_InvalidToken() {
        String token = "InvalidToken";

        LogoutResponseDTO response = authService.logout(token);

        assertEquals("Logout successfully", response.message());
        verify(userSessionRepository, never()).deleteByToken(anyString());
    }
}