package com.kitchen.sink.service.impl;

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
import com.kitchen.sink.exception.ValidationException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.utils.JWTUtils;
import com.kitchen.sink.utils.UniversalConverter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UniversalConverter convertor;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    @Transactional
    public UserResDTO saveUser(UserReqDTO userDTO) {
        log.info("Saving User: {}", userDTO);
        validateUserRoleAtLeastOneRole(userDTO);
        User user = convertor.convert(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        setAuditFieldsForCreate(user);
        userRepository.save(user);
        log.info("User saved: {}", user);
        return convertor.convert(user, UserResDTO.class);
    }

    @Override
    public UserResDTO getUserById(String id) {
        log.info("Fetching User by ID: {}", id);
        User user = findUserById(id);
        log.info("User fetched: {}", user);
        return convertor.convert(user, UserResDTO.class);
    }

    @Override
    public UserResDTO getUserByEmail(String email) {
        log.info("Fetching User by Email: {}", email);
        User user = findUserByEmail(email);
        log.info("User fetched: {}", user);
        return convertor.convert(user, UserResDTO.class);
    }

    @Override
    public UserDTO getUserDTOByEmail(String email) {
        log.info("Fetching UserDTO by Email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("User not found with Email: " + email, HttpStatus.BAD_REQUEST));
            if (!member.getStatus().equals(MemberStatus.APPROVED)) {
                throw new ValidationException("Application on " + member.getStatus() + " status", HttpStatus.BAD_REQUEST);
            }
        }
        if (user.isPresent()) {
            if (user.get().getStatus().equals(UserStatus.BLOCKED)) {
                throw new ValidationException("User is blocked", HttpStatus.BAD_REQUEST);
            }
            return convertor.convert(user.get(), UserDTO.class);
        }
        return null;
    }

    @Override
    @Transactional
    public UserResDTO updateUser(UserReqDTO userDTO) {
        log.info("Updating User: {}", userDTO);
        validateUserRoleAtLeastOneRole(userDTO);
        User user = findUserById(userDTO.id());
        User userToBeUpdate = convertor.convert(userDTO, User.class);
        setAuditFieldsForUpdate(userToBeUpdate, user);
        userRepository.save(userToBeUpdate);
        log.info("User updated: {}", userToBeUpdate);
        return convertor.convert(userToBeUpdate, UserResDTO.class);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting User by ID: {}", id);
        User user = findUserById(id);
        userRepository.deleteById(id);
        memberRepository.deleteByEmail(user.getEmail());
        log.info("User deleted with ID: {}", id);
    }

    @Override
    public List<UserResV1DTO> getAllUsers() {
        log.info("Fetching all Users");
        List<User> users = userRepository.findAll();
        List<UserResV1DTO> userDTOs = users.stream()
                .peek(user -> user.setPassword(null))
                .map(user -> convertor.convert(user, UserResV1DTO.class))
                .collect(Collectors.toList());
        log.info("All Users fetched");
        return userDTOs;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO) {
        log.info("Resetting password for User with ID: {}", resetPasswordReqDTO.id());
        User user = findUserById(resetPasswordReqDTO.id());
        user.setPassword(passwordEncoder.encode(resetPasswordReqDTO.password()));
        setAuditFieldsForUpdate(user, user);
        userRepository.save(user);
        log.info("Password reset for User with ID: {}", resetPasswordReqDTO.id());
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordResDTO changePasswordResDTO) {
        String currentUser = jwtUtils.getEmail();
        log.info("Changing password for current User: {}", currentUser);
        User user = findUserByEmail(currentUser);
        if (!passwordEncoder.matches(changePasswordResDTO.currentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(changePasswordResDTO.newPassword()));
        setAuditFieldsForUpdate(user, user);
        userRepository.save(user);
        log.info("Password changed for User: {}", currentUser);
    }

    @Override
    @Transactional
    public void changeUserActivationStatus(UserStatus status, String id) {
        log.info("Changing activation status for User with ID: {} to {}", id, status);
        User user = findUserById(id);
        user.setStatus(status);
        userRepository.save(user);
        log.info("Activation status changed for User with ID: {} to {}", id, status);
    }

    private void validateUserRoleAtLeastOneRole(UserReqDTO userDTO) {
        log.debug("Validating User roles: {}", userDTO.roles());
        if (userDTO.roles() == null || userDTO.roles().isEmpty()) {
            throw new ValidationException("User must have at least one role", HttpStatus.BAD_REQUEST);
        }
        if (!userDTO.roles().contains(UserRole.VISITOR)) {
            throw new ValidationException("User should have role VISITOR", HttpStatus.BAD_REQUEST);
        }
    }

    private User findUserById(String id) {
        log.debug("Finding User by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id, HttpStatus.BAD_REQUEST));
    }

    private User findUserByEmail(String email) {
        log.debug("Finding User by Email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with Email: " + email, HttpStatus.BAD_REQUEST));
    }

    private void setAuditFieldsForCreate(User user) {
        log.debug("Setting audit fields for create for User: {}", user);
        String createdBy = jwtUtils.getEmail();
        user.setCreatedBy(createdBy);
        user.setCreatedTime(LocalDateTime.now());
        user.setLastModifiedBy(createdBy);
        user.setLastModifiedTime(user.getCreatedTime());
    }

    private void setAuditFieldsForUpdate(User user, User existingUser) {
        log.debug("Setting audit fields for update for User: {}", user);
        String updatedBy = jwtUtils.getEmail();
        user.setLastModifiedBy(updatedBy);
        user.setLastModifiedTime(LocalDateTime.now());
        user.setCreatedBy(existingUser.getCreatedBy());
        user.setCreatedTime(existingUser.getCreatedTime());
        user.setPassword(existingUser.getPassword());
    }
}