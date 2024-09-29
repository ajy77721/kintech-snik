package com.kitchen.sink.service.impl;

import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.dto.UserDetailsDTO;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user by email: {}", email);
        UserDTO user = userService.getUserDTOByEmail(email);
        if (user == null) {
            log.error("User not found with email: {}", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        if (user.status().equals(UserStatus.BLOCKED)) {
            log.error("User is blocked: {}", email);
            throw new LockedException("User is blocked: " + email + ". Please contact support.");
        }
        log.info("User found: {}", user);
        return new UserDetailsDTO(user);
    }
}