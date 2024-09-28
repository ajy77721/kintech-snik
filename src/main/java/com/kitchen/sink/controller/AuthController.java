package com.kitchen.sink.controller;

import com.kitchen.sink.dto.*;
import com.kitchen.sink.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Log in user", description = "Log in user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login success full", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {LoginResponseDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PostMapping("/login")
    public ResponseEntity<APIResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(APIResponseDTO.<LoginResponseDTO>builder()
                        .status(true).
                        data(authService.login(loginRequest)).
                        build());

    }

    @Operation(summary = "Log out user", description = "Log out user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout success full",
                    content = @Content(schema =@Schema(
                    implementation = APIResponseDTO.class,
                    oneOf = {LogoutResponseDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PostMapping("/logout")
    public ResponseEntity<APIResponseDTO<LogoutResponseDTO>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        return ResponseEntity.ok(APIResponseDTO.<LogoutResponseDTO>builder()
                .status(true)
                .data(authService.logout(authorizationHeader))
                .build());
    }
}
