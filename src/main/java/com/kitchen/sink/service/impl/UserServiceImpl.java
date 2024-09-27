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
import com.kitchen.sink.utils.JwtUtil;
import com.kitchen.sink.utils.ObjectConvertor;
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
    private ObjectConvertor convertor;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private MemberRepository memberRepository;

    @Override
    @Transactional
    public UserResDTO saveUser(UserReqDTO userDTO) {
        log.info("Saving User: {}", userDTO);
        validateUserRoleAtLeastOneRole(userDTO);
        User user = convertor.convert(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String createdBy = jwtUtil.getEmail();
        if (createdBy == null) {
            createdBy = user.getEmail();
        }
        user.setCreatedBy(createdBy);
        user.setCreatedTime(LocalDateTime.now());
        user.setLastModifiedBy(createdBy);
        user.setLastModifiedTime(user.getCreatedTime());
        userRepository.save(user);
        log.info("User saved: {}", user);
        return convertor.convert(user, UserResDTO.class);
    }

    @Override
    public UserResDTO getUserById(String id) {
        log.info("Fetching User by ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with ID: " + id, HttpStatus.BAD_REQUEST);
        }
        return convertor.convert(user.get(), UserResDTO.class);
    }

    @Override
    public UserResDTO getUserByEmail(String email) {
        log.info("Fetching User by Email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new NotFoundException("User not found with Email: " + email, HttpStatus.BAD_REQUEST);
        }
        return convertor.convert(user.get(), UserResDTO.class);
    }
    @Override
    public UserDTO getUserDTOByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
           Member member= memberRepository.findByEmail(email)
                   .orElseThrow(() ->
                           new NotFoundException("User not found with Email: " + email, HttpStatus.BAD_REQUEST));
            if(!member.getStatus().equals(MemberStatus.APPROVED)){
                throw new ValidationException("Application on "+member.getStatus()+" status",HttpStatus.BAD_REQUEST);
            }
        }
        if (user.isPresent()){
        if (user.get().getStatus().equals(UserStatus.BLOCKED)){
            throw new ValidationException("User is blocked",HttpStatus.BAD_REQUEST);
        }
        return convertor.convert(user.get(), UserDTO.class);
        }
        return null;
    }

    @Override
    @Transactional
    public UserResDTO updateUser(UserReqDTO userDTO) {
        validateUserRoleAtLeastOneRole(userDTO);
        log.info("Updating User: {}", userDTO);
        User user = userRepository.findById(userDTO.id())
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userDTO.id(), HttpStatus.BAD_REQUEST));
        User userToBeUpdate = convertor.convert(userDTO, User.class);
        userToBeUpdate.setPassword(user.getPassword());
        userToBeUpdate.setCreatedTime(user.getCreatedTime());
        userToBeUpdate.setCreatedBy(user.getCreatedBy());
        String updateBy = jwtUtil.getEmail();
        if (updateBy == null) {
            updateBy = user.getEmail();
        }
        userToBeUpdate.setLastModifiedBy(updateBy);
        userToBeUpdate.setLastModifiedTime(LocalDateTime.now());
        userToBeUpdate.setStatus(user.getStatus());
        userRepository.save(userToBeUpdate);
        log.info("User updated: {}", userToBeUpdate);
        return convertor.convert(userToBeUpdate, UserResDTO.class);
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        log.info("Deleting User by ID: {}", id);
        User user=userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id, HttpStatus.BAD_REQUEST));
        userRepository.deleteById(id);
        memberRepository.deleteByEmail(user.getEmail());

    }

    @Override
    public List<UserResV1DTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().peek(user -> user.setPassword(null)).map(user -> convertor.convert(user, UserResV1DTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO) {
        User user = userRepository.findById(resetPasswordReqDTO.id())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + resetPasswordReqDTO.id(), HttpStatus.BAD_REQUEST));
        user.setPassword(passwordEncoder.encode(resetPasswordReqDTO.password()));
        user.setLastModifiedTime(LocalDateTime.now());
        user.setLastModifiedBy(jwtUtil.getEmail());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordResDTO changePasswordResDTO) {
        String currentUser = jwtUtil.getEmail();
        User user = userRepository.findByEmail(currentUser)
                .orElseThrow(() -> new NotFoundException("User not found with Email: " + currentUser, HttpStatus.BAD_REQUEST));
        if (!passwordEncoder.matches(changePasswordResDTO.currentPassword(), user.getPassword())) {
            throw  new ValidationException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(changePasswordResDTO.newPassword()));
        user.setLastModifiedTime(LocalDateTime.now());
        user.setLastModifiedBy(currentUser);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changeUserActivationStatus(UserStatus status, String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id, HttpStatus.BAD_REQUEST));
        user.setStatus(status);
        userRepository.save(user);

    }

    private void validateUserRoleAtLeastOneRole(UserReqDTO userDTO) {
        if (userDTO.roles() == null || userDTO.roles().isEmpty()) {
            throw new ValidationException("User must have at least one role", HttpStatus.BAD_REQUEST);
        }
        if (!userDTO.roles().contains(UserRole.VISITOR)){
            throw new ValidationException("User should have role VISITOR",HttpStatus.BAD_REQUEST);
        }
    }


}
