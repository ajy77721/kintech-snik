package com.kitchen.sink.controller;

import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.response.MemberDTO;
import com.kitchen.sink.dto.response.MemberResDTO;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.service.MemberService;
import com.kitchen.sink.validation.CreateGroup;
import com.kitchen.sink.validation.RegisterGroup;
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
import java.util.Set;


@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Operation(summary = "Add Member", description = "Add a new member to the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Add a new member to the group success", content = @Content(schema =@Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {MemberResDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ResponseEntity<APIResponseDTO<MemberResDTO>> addMember(@RequestBody @NotNull(message = "MemberDTO cannot be null") @Validated(CreateGroup.class) MemberReqDTO memberReqDTO) {
        APIResponseDTO<MemberResDTO> apiResponse = APIResponseDTO.<MemberResDTO>builder().status(true).data(memberService.saveMember(memberReqDTO)).build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Member", description = "Get member details by member id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get member details by member id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {MemberReqDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @GetMapping("/id/{memberId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<APIResponseDTO<MemberResDTO>> getMembers(@PathVariable String memberId) {
        return ResponseEntity.ok().body(APIResponseDTO.<MemberResDTO>builder().status(true).data(memberService.getMember(memberId)).build());
    }
    @Operation(summary = "Update Member", description = "Update member details by member id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update member details by member id success", content = @Content(schema =@Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {MemberReqDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping
    public ResponseEntity<APIResponseDTO<MemberResDTO>> updateMember(@Validated(UpdateGroup.class) @RequestBody @NotNull(message = "cannot be null") MemberReqDTO memberReqDTO) {
        return ResponseEntity.ok().body(APIResponseDTO.<MemberResDTO>builder().status(true).data(memberService.updateMember(memberReqDTO)).build());
    }
    @Operation(summary = "Delete Member", description = "Delete member by member id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Delete member by member id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<APIResponseDTO<String>> deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Member Removed Successfully").build());
    }

    @Operation(summary = "Change Member Status", description = "Change member status by member id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change member status by member id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/change-status")
    public ResponseEntity<APIResponseDTO<String>> changeMemberStatus(@RequestParam @NotBlank(message = "memberId can not be blank") String memberId,
                                                                     @RequestParam @NotNull(message = "memberStatus can not be null")  MemberStatus status,
                                                                     @RequestParam(required = false) Set<UserRole> userRoles) {
        memberService.changeMemberStatus(memberId,status,userRoles);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Member Status Update Successfully").build());
    }

    @Operation(summary = "Register Member", description = "Register a new member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Register a new member success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PostMapping("/register")
    public ResponseEntity<APIResponseDTO<String>> registerMember(@RequestBody @NotNull(message = "MemberDTO cannot be null") @Validated(RegisterGroup.class)  MemberReqDTO memberReqDTO) {
        memberService.registerMember(memberReqDTO);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Registration successful. Pending approval").build());
    }

    @Operation(summary = "Get All Members", description = "Get all members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get all members success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {MemberDTO.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','USER','VISITOR')")
    public ResponseEntity<APIResponseDTO<List<MemberDTO>>> getAllMembers() {
        return ResponseEntity.ok().body(APIResponseDTO.<List<MemberDTO>>builder().status(true).data(memberService.getAllMembers()).build());
    }

    @Operation(summary = "Reset Password", description = "Reset member password by member id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reset member password by member id success", content = @Content(schema = @Schema(
                    implementation = APIResponseDTO.class,
                    subTypes = {String.class}
            ))),
            @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)

    })
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/reset-password")
    public ResponseEntity<APIResponseDTO<String>> resetPassword(@RequestBody @Validated @NotNull(message = "Can not be null") ResetPasswordReqDTO resetPasswordReqDTO) {
        memberService.resetPassword(resetPasswordReqDTO);
        return ResponseEntity.ok().body(APIResponseDTO.<String>builder().status(true).data("Member Password Update Successfully").build());
    }
}
