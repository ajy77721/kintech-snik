package com.kitchen.sink.controller;

import com.kitchen.sink.dto.*;
import com.kitchen.sink.entity.UserSession;
import com.kitchen.sink.repo.UserSessionRepository;
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionRepository userSessionRepository;

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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
        );
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("invalid user request !");
        }
        UserDTO user = userService.getUserDTOByEmail(loginRequest.email());
        UserSession byUsername = userSessionRepository.findByUsername(user.email());
        if (byUsername != null) {
            if (jwtUtil.validateToken(byUsername.getToken(), user.email())) {
                return ResponseEntity.ok(APIResponseDTO.<LoginResponseDTO>builder().status(true).data(LoginResponseDTO.builder().token(byUsername.getToken()).email(user.email()).build()).build());
            }
            userSessionRepository.deleteById(byUsername.getId());
        }

        String jwt = jwtUtil.generateToken(user.email(), user.roles());
        userSessionRepository.save(UserSession.builder().username(user.email()).token(jwt).build());
        return ResponseEntity.ok(APIResponseDTO.<LoginResponseDTO>builder().status(true).data(LoginResponseDTO.builder().token(jwt).email(user.email()).build()).build());

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
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            userSessionRepository.deleteByToken(jwt);
        }
        APIResponseDTO<LogoutResponseDTO> build =  APIResponseDTO.<LogoutResponseDTO>builder().status(true)
                .data(LogoutResponseDTO.builder()
                        .message("Logout successfully !").build()).build();
        return ResponseEntity.ok(build);
    }
}
