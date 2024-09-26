package com.kitchen.sink.service;


import com.kitchen.sink.dto.LoginRequestDTO;
import com.kitchen.sink.dto.LoginResponseDTO;
import com.kitchen.sink.dto.LogoutResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    LogoutResponseDTO logout(String token);
}
