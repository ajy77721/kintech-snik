package com.kitchen.sink.service;

import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.response.MemberDTO;
import com.kitchen.sink.dto.response.MemberResDTO;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.enums.UserRole;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Set;

public interface MemberService {
    MemberResDTO saveMember(MemberReqDTO memberReqDTO);

    MemberResDTO getMember(String  id);

    MemberResDTO updateMember(MemberReqDTO memberReqDTO);

    void deleteMember(String id);

    void changeMemberStatus(String memberId, MemberStatus status, Set<UserRole> userRoles);

    void registerMember(MemberReqDTO memberReqDTO);

    List<MemberDTO> getAllMembers();

    void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO);
}
