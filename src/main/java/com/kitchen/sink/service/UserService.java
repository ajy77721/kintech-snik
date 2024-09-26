package com.kitchen.sink.service;

import com.kitchen.sink.dto.UserDTO;
import com.kitchen.sink.dto.request.ChangePasswordResDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.request.UserReqDTO;
import com.kitchen.sink.dto.response.UserResDTO;
import com.kitchen.sink.dto.response.UserResV1DTO;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.enums.UserStatus;

import java.util.List;

public interface UserService {
    UserResDTO saveUser(UserReqDTO user);
    UserResDTO getUserById(String id);
    UserResDTO getUserByEmail(String email);
    UserDTO getUserDTOByEmail(String email);
    UserResDTO updateUser(UserReqDTO user);
    void deleteUser(String id);
    List<UserResV1DTO> getAllUsers();

    void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO);

    void changePassword(ChangePasswordResDTO changePasswordResDTO);

    void changeUserActivationStatus(UserStatus status,String id);
}
