package com.kitchen.sink.service.impl;

import com.kitchen.sink.config.MasterTokenConfig;
import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.dto.UserDetailsDTO;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
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
            log.error("User not found with username: {}", email);
            throw new UsernameNotFoundException("User not found with username: " + email);
        }
        log.info("User found with user: {}", user);
        return new UserDetailsDTO(user);
    }
}
