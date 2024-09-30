package com.kitchen.sink.service;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.dto.request.ChangePasswordResDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.request.UserReqDTO;
import com.kitchen.sink.dto.response.UserResDTO;
import com.kitchen.sink.dto.response.UserResV1DTO;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.exception.NotFoundException;
import com.kitchen.sink.exception.ObjectMappingException;
import com.kitchen.sink.exception.SinkValidationException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.impl.UserServiceImpl;
import com.kitchen.sink.utils.JWTUtils;
import com.kitchen.sink.utils.UniversalConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UniversalConverter convertor;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private MemberRepository memberRepository;


    @BeforeEach
    void setUp() {
        // Initialize mocks
    }

    @Test
    void testSaveUser_Successful() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Test User", "test@example.com", "12312","password", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setCreatedBy("test@example.com");
        user.setCreatedTime(LocalDateTime.now());

        when(convertor.convert(userReqDTO, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");

        UserResDTO userResDTO = new UserResDTO("1", "Test User", "test@example.com","1312", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);
        when(convertor.convert(user, UserResDTO.class)).thenReturn(userResDTO);

        UserResDTO result = userService.saveUser(userReqDTO);

        verify(userRepository, times(1)).save(user);
        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("test@example.com", result.email());
    }

    @Test
    void testSaveUser_NoRoles() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Test User", "test@example.com", "12312","password", Set.of(), UserStatus.ACTIVE);

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.saveUser(userReqDTO));
        assertEquals("User must have at least one role", exception.getMessage());
    }

    @Test
    void testSaveUser_NoVisitorRole() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Test User", "test@example.com", "12345", "password",Set.of(UserRole.ADMIN), UserStatus.ACTIVE);
        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.saveUser(userReqDTO));
        assertEquals("User should have role VISITOR", exception.getMessage());
    }

    @Test
    void testSaveUser_DuplicateKeyException() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Test User", "test@example.com", "12312","password", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setCreatedBy("test@example.com");
        user.setCreatedTime(LocalDateTime.now());

        when(convertor.convert(userReqDTO, User.class)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");

        when(userRepository.save(user)).thenThrow(new DuplicateKeyException("User with email"));
        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> userService.saveUser(userReqDTO));
        assertEquals("User with email", exception.getMessage());
    }

    @Test
    void testSaveUser_ObjectMappingException() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Test User", "test@example.com", "1321", "password",Set.of(UserRole.VISITOR), UserStatus.ACTIVE);

        when(convertor.convert(userReqDTO, User.class)).thenThrow(new ObjectMappingException("Error converting object"));

        ObjectMappingException exception = assertThrows(ObjectMappingException.class, () -> userService.saveUser(userReqDTO));
        assertEquals("Error converting object", exception.getMessage());
    }
    @Test
    void testGetUserById_Successful() {
        String userId = "1";
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        UserResDTO userResDTO = new UserResDTO(userId, "Test User", "test@example.com", "12312", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(convertor.convert(user, UserResDTO.class)).thenReturn(userResDTO);

        UserResDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        assertEquals("test@example.com", result.email());
    }

    @Test
    void testGetUserById_UserNotFound() {
        String userId = "1";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
    @Test
    void testGetUserByEmail_Successful() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        UserResDTO userResDTO = new UserResDTO("1", "Test User", email, "12312", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(convertor.convert(user, UserResDTO.class)).thenReturn(userResDTO);

        UserResDTO result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.email());
    }

    @Test
    void testGetUserByEmail_UserNotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserByEmail(email));
        assertEquals("User not found with Email: " + email, exception.getMessage());
    }
    @Test
    void testGetUserDTOByEmail_Successful() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);

        UserDTO userDTO = new UserDTO("1", "Test User", email, "password",Set.of(), UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(convertor.convert(user, UserDTO.class)).thenReturn(userDTO);

        UserDTO result = userService.getUserDTOByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.email());
    }

    @Test
    void testGetUserDTOByEmail_UserNotFound() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.getUserDTOByEmail(email));
        assertEquals("User not found with Email: " + email, exception.getMessage());
    }

    @Test
    void testGetUserDTOByEmail_UserBlocked() {
        String email = "blocked@example.com";
        User user = new User();
        user.setEmail(email);
        user.setStatus(UserStatus.BLOCKED);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.getUserDTOByEmail(email));
        assertEquals("User is blocked", exception.getMessage());
    }

    @Test
    void testGetUserDTOByEmail_MemberNotApproved() {
        String email = "member@example.com";
        Member member = new Member();
        member.setEmail(email);
        member.setStatus(MemberStatus.PENDING);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.getUserDTOByEmail(email));
        assertEquals("Application on PENDING status", exception.getMessage());
    }
    @Test
    void testUpdateUser_Successful() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Updated User", "updated@example.com", "12312", "password", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);
        User existingUser = new User();
        existingUser.setId("1");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("encodedPassword");
        existingUser.setCreatedBy("test@example.com");
        existingUser.setCreatedTime(LocalDateTime.now());

        User updatedUser = new User();
        updatedUser.setId("1");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPassword("encodedPassword");
        updatedUser.setCreatedBy("test@example.com");
        updatedUser.setCreatedTime(existingUser.getCreatedTime());
        updatedUser.setLastModifiedBy("test@example.com");
        updatedUser.setLastModifiedTime(LocalDateTime.now());
        updatedUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userReqDTO.id())).thenReturn(Optional.of(existingUser));
        when(convertor.convert(userReqDTO, User.class)).thenReturn(updatedUser);
        when(jwtUtils.getEmail()).thenReturn("test@example.com");

        UserResDTO userResDTO = new UserResDTO("1", "Updated User", "updated@example.com", "12312", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);
        when(convertor.convert(updatedUser, UserResDTO.class)).thenReturn(userResDTO);

        UserResDTO result = userService.updateUser(userReqDTO);

        verify(userRepository, times(1)).save(updatedUser);
        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("updated@example.com", result.email());
    }

    @Test
    void testUpdateUser_UserNotFound() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Updated User", "updated@example.com", "12312", "password", Set.of(UserRole.VISITOR), UserStatus.ACTIVE);

        when(userRepository.findById(userReqDTO.id())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateUser(userReqDTO));
        assertEquals("User not found with ID: " + userReqDTO.id(), exception.getMessage());
    }

    @Test
    void testUpdateUser_NoRoles() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Updated User", "updated@example.com", "12312", "password", Set.of(), UserStatus.ACTIVE);

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.updateUser(userReqDTO));
        assertEquals("User must have at least one role", exception.getMessage());
    }

    @Test
    void testUpdateUser_NoVisitorRole() {
        UserReqDTO userReqDTO = new UserReqDTO("1", "Updated User", "updated@example.com", "12312", "password", Set.of(UserRole.ADMIN), UserStatus.ACTIVE);

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.updateUser(userReqDTO));
        assertEquals("User should have role VISITOR", exception.getMessage());
    }
    @Test
    void testDeleteUser_Successful() {
        String userId = "1";
        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository, times(1)).deleteById(userId);
        verify(memberRepository, times(1)).deleteByEmail(user.getEmail());
    }

    @Test
    void testDeleteUser_UserNotFound() {
        String userId = "1";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
    @Test
    void testGetAllUsers_Successful() {
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);

        UserResV1DTO userResV1DTO = new UserResV1DTO("1", "Test User", "test@example.com", "12312", Set.of(UserRole.VISITOR), null,null,"","",UserStatus.ACTIVE);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(convertor.convert(user, UserResV1DTO.class)).thenReturn(userResV1DTO);

        List<UserResV1DTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@example.com", result.get(0).email());
    }

    @Test
    void testGetAllUsers_NoUsersFound() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResV1DTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void testResetPassword_Successful() {
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO("1", "newPassword");
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setPassword("oldEncodedPassword");

        when(userRepository.findById(resetPasswordReqDTO.id())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(resetPasswordReqDTO.password())).thenReturn("newEncodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");

        userService.resetPassword(resetPasswordReqDTO);

        verify(userRepository, times(1)).save(user);
        assertEquals("newEncodedPassword", user.getPassword());
        assertEquals("test@example.com", user.getLastModifiedBy());
        assertNotNull(user.getLastModifiedTime());
    }

    @Test
    void testResetPassword_UserNotFound() {
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO("1", "newPassword");

        when(userRepository.findById(resetPasswordReqDTO.id())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.resetPassword(resetPasswordReqDTO));
        assertEquals("User not found with ID: " + resetPasswordReqDTO.id(), exception.getMessage());
    }
    @Test
    void testChangePassword_Successful() {
        ChangePasswordResDTO changePasswordResDTO = new ChangePasswordResDTO("currentPassword", "newPassword");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedCurrentPassword");

        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encodedCurrentPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        userService.changePassword(changePasswordResDTO);

        verify(userRepository, times(1)).save(user);
        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals("test@example.com", user.getLastModifiedBy());
        assertNotNull(user.getLastModifiedTime());
    }

    @Test
    void testChangePassword_UserNotFound() {
        ChangePasswordResDTO changePasswordResDTO = new ChangePasswordResDTO("currentPassword", "newPassword");

        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.changePassword(changePasswordResDTO));
        assertEquals("User not found with Email: test@example.com", exception.getMessage());
    }

    @Test
    void testChangePassword_IncorrectCurrentPassword() {
        ChangePasswordResDTO changePasswordResDTO = new ChangePasswordResDTO("currentPassword", "newPassword");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedCurrentPassword");

        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPassword", "encodedCurrentPassword")).thenReturn(false);

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> userService.changePassword(changePasswordResDTO));
        assertEquals("Current password is incorrect", exception.getMessage());
    }
    @Test
    void testChangeUserActivationStatus_Successful() {
        String userId = "1";
        User user = new User();
        user.setId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.changeUserActivationStatus(UserStatus.BLOCKED, userId);

        verify(userRepository, times(1)).save(user);
        assertEquals(UserStatus.BLOCKED, user.getStatus());
    }

    @Test
    void testChangeUserActivationStatus_UserNotFound() {
        String userId = "1";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.changeUserActivationStatus(UserStatus.BLOCKED, userId));
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
}