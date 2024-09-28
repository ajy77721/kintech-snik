package com.kitchen.sink.service;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.dto.UserDetailsDTO;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        // Initialize mocks
    }

    @Test
    void testLoadUserByUsername_Successful() {
        String email = "test@example.com";
        UserDTO userDTO = new UserDTO("1", "Test User", email, "password", Set.of(UserRole.USER), UserStatus.ACTIVE);

        when(userService.getUserDTOByEmail(email)).thenReturn(userDTO);

        UserDetailsDTO userDetails = (UserDetailsDTO) userDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String email = "notfound@example.com";

        when(userService.getUserDTOByEmail(email)).thenReturn(null);

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
        assertEquals("User not found with username: " + email, exception.getMessage());
    }

    @Test
    void testLoadUserByUsername_UserBlocked() {
        String email = "blocked@example.com";
        UserDTO userDTO = new UserDTO("1", "Blocked User", email, "password", Set.of(UserRole.VISITOR),UserStatus.BLOCKED);

        when(userService.getUserDTOByEmail(email)).thenReturn(userDTO);

        LockedException exception = assertThrows(LockedException.class, () -> userDetailsService.loadUserByUsername(email));
        assertEquals("User is blocked: " + email + " Please contact support", exception.getMessage());
    }
}