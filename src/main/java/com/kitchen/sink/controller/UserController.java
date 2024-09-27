package com.kitchen.sink.controller;

import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.request.ChangePasswordResDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.request.UserReqDTO;
import com.kitchen.sink.dto.response.UserResDTO;
import com.kitchen.sink.dto.response.UserResV1DTO;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.validation.CreateGroup;
import com.kitchen.sink.validation.UpdateGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Save User", description = "Save a new user to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Save a new user to the database success", content = @Content(schema =
            @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {UserResDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<UserResDTO>> saveUser(@RequestBody @NotNull(message = "UserDTO cannot be null") @Validated(CreateGroup.class) UserReqDTO userDTO) {
        UserResDTO savedUser = userService.saveUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponseDTO.<UserResDTO>builder().status(true).data(savedUser).build());
    }

    @Operation(summary = "Get User", description = "Get user details by user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user details by user id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {UserResDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})

    @GetMapping("/id/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER','VISITOR')")
    public ResponseEntity<APIResponseDTO<UserResDTO>> getUserById(@PathVariable @NotBlank(message = "Id Can not be blank") String id) {
        UserResDTO user = userService.getUserById(id);
        return ResponseEntity.ok(APIResponseDTO.<UserResDTO>builder().status(true).data(user).build());
    }

    @Operation(summary = "Get User", description = "Get user details by user email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user details by user email success", content = @Content(schema =@Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {UserResDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER','VISITOR')")
    public ResponseEntity<APIResponseDTO<UserResDTO>> getUserByEmail(@PathVariable  @NotBlank(message = "Id Can not be blank") String email) {
        UserResDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(APIResponseDTO.<UserResDTO>builder().status(true).data(user).build());
    }

    @Operation(summary = "Update User", description = "Update user details by user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update user details by user id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {UserResDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})

    @PutMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<UserResDTO>> updateUser(@RequestBody @NotNull(message = "cannot be null") @Validated(UpdateGroup.class) UserReqDTO userDTO) {
        UserResDTO updatedUser = userService.updateUser(userDTO);
        return ResponseEntity.ok(APIResponseDTO.<UserResDTO>builder().status(true).data(updatedUser).build());
    }

    @Operation(summary = "Delete User", description = "Delete user by user id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete user by user id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<String>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("delete successfully").build());
    }

    @Operation(summary = "Get All Users", description = "Get all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get all users success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes ={List.class, UserResV1DTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<List<UserResV1DTO>>> getAll() {
        return ResponseEntity.ok().body(APIResponseDTO.<List<UserResV1DTO>>builder().status(true).data(userService.getAllUsers()).build());
    }

    @Operation(summary = "Reset Password", description = "Reset password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset password success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})
    @PostMapping("/reset-password")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<String>> resetPassword(@RequestBody @NotNull(message = "cannot be null") @Validated ResetPasswordReqDTO  resetPasswordReqDTO) {
        userService.resetPassword(resetPasswordReqDTO);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Password reset successfully").build());
    }

    @Operation(summary = "Change Password", description = "Change password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change password success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})
    @PostMapping("/change-password")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER','VISITOR')")
    public ResponseEntity<APIResponseDTO<String>> changePassword(@RequestBody @NotNull(message = "cannot be null") @Validated ChangePasswordResDTO changePasswordResDTO) {
        userService.changePassword(changePasswordResDTO);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Password changed successfully").build());
    }

    @Operation(summary = "Change User Activation Status", description = "Change user activation status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change user activation status success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)})
    @PostMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<String>> changeUserActivationStatus(@PathVariable("status") @NotNull(message = "cannot be null") UserStatus status, @PathVariable("id") @NotNull(message = "cannot be null") String id) {
        userService.changeUserActivationStatus(status,id);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("User status changed successfully").build());
    }
}
