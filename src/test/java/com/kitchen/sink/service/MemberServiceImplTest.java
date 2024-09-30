package com.kitchen.sink.service;

import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.response.MemberDTO;
import com.kitchen.sink.dto.response.MemberResDTO;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.exception.NotFoundException;
import com.kitchen.sink.exception.ObjectMappingException;
import com.kitchen.sink.exception.SinkValidationException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.impl.MemberServiceImpl;
import com.kitchen.sink.utils.JWTUtils;
import com.kitchen.sink.utils.UniversalConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UniversalConverter convertor;

    @Mock
    private JWTUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveMember_Successful() {
        MemberReqDTO memberReqDTO = new MemberReqDTO(null,"test@example.com", "password", "Test User", "password", null);
        Member member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("encodedPassword");
        member.setCreatedBy("test@example.com");
        member.setCreatedTime(LocalDateTime.now());

        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(convertor.convert(any(Member.class), eq(MemberResDTO.class))).thenReturn(new MemberResDTO("Id","test@example.com", "password", "Test User",  null));
        MemberResDTO response = memberService.saveMember(memberReqDTO);

        assertNotNull(response);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testGetMember_Successful() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(convertor.convert(any(Member.class), eq(MemberResDTO.class))).thenReturn(new MemberResDTO("1", "Test User", "test@example.com", "1234567890", null));

        MemberResDTO response = memberService.getMember(memberId);

        assertNotNull(response);
        assertEquals("1", response.id());
        assertEquals("Test User", response.name());
        verify(memberRepository, times(1)).findById(memberId);
    }

    @Test
    void testSaveMember_DuplicateKeyException() {
        MemberReqDTO memberReqDTO = new MemberReqDTO(null, "test@example.com", "password", "Test User", "password", null);
        Member member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("encodedPassword");
        member.setCreatedBy("test@example.com");
        member.setCreatedTime(LocalDateTime.now());

        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(memberRepository.save(any(Member.class))).thenThrow(new DuplicateKeyException("Duplicate key error"));

        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> memberService.saveMember(memberReqDTO));
        assertEquals("Duplicate key error", exception.getMessage());
    }

    @Test
    void testSaveMember_ObjectMappingException() {
        MemberReqDTO memberReqDTO = new MemberReqDTO(null, "test@example.com", "password", "Test User", "password", null);
        Member member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("encodedPassword");
        member.setCreatedBy("test@example.com");
        member.setCreatedTime(LocalDateTime.now());

        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtils.getEmail()).thenReturn("test@example.com");
        when(memberRepository.save(any(Member.class))).thenReturn(member);
        when(convertor.convert(any(Member.class), eq(MemberResDTO.class))).thenThrow(new ObjectMappingException("Error converting object"));

        ObjectMappingException exception = assertThrows(ObjectMappingException.class, () -> memberService.saveMember(memberReqDTO));
        assertEquals("Error converting object", exception.getMessage());
    }

    @Test
    void testGetMember_NotFound() {
        String memberId = "1";

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> memberService.getMember(memberId));
        assertEquals("No Member found with ID: 1", exception.getMessage());
    }

    @Test
    void testGetMember_NullId() {
        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.getMember(null));
        assertEquals("Member Id cannot be null", exception.getMessage());
    }
    @Test
    void testUpdateMember_Successful() {
        MemberReqDTO memberReqDTO = new MemberReqDTO("1", "test@example.com", "password", "Test User", "password", null);
        Member existingMember = new Member();
        existingMember.setId("1");
        existingMember.setStatus(MemberStatus.PENDING);
        existingMember.setCreatedBy("creator@example.com");
        existingMember.setCreatedTime(LocalDateTime.now());

        Member updatedMember = new Member();
        updatedMember.setId("1");
        updatedMember.setEmail("test@example.com");
        updatedMember.setName("Test User");
        updatedMember.setStatus(MemberStatus.PENDING);
        updatedMember.setCreatedBy("creator@example.com");
        updatedMember.setCreatedTime(LocalDateTime.now());
        updatedMember.setLastModifiedBy("modifier@example.com");
        updatedMember.setLastModifiedTime(LocalDateTime.now());

        when(memberRepository.findById("1")).thenReturn(Optional.of(existingMember));
        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(updatedMember);
        when(jwtUtils.getEmail()).thenReturn("modifier@example.com");
        when(memberRepository.save(any(Member.class))).thenReturn(updatedMember);
        when(convertor.convert(any(Member.class), eq(MemberResDTO.class))).thenReturn(new MemberResDTO("1", "Test User","test@example.com", "password",  null));

        MemberResDTO response = memberService.updateMember(memberReqDTO);

        assertNotNull(response);
        assertEquals("1", response.id());
        assertEquals("test@example.com", response.email());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testUpdateMember_NotFound() {
        MemberReqDTO memberReqDTO = new MemberReqDTO("1", "test@example.com", "password", "Test User", "password", null);

        when(memberRepository.findById("1")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> memberService.updateMember(memberReqDTO));
        assertEquals("No Member found with ID: 1", exception.getMessage());
    }

    @Test
    void testUpdateMember_AlreadyApprovedOrDeclined() {
        MemberReqDTO memberReqDTO = new MemberReqDTO("1", "test@example.com", "password", "Test User", "password", null);
        Member existingMember = new Member();
        existingMember.setId("1");
        existingMember.setStatus(MemberStatus.APPROVED);

        when(memberRepository.findById("1")).thenReturn(Optional.of(existingMember));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.updateMember(memberReqDTO));
        assertEquals("Member is already approved/declined, cannot update", exception.getMessage());
    }
    @Test
    void testDeleteMember_Successful() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.PENDING);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.empty());

        memberService.deleteMember(memberId);

        verify(memberRepository, times(1)).deleteById(memberId);
    }

    @Test
    void testDeleteMember_NotFound() {
        String memberId = "1";

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> memberService.deleteMember(memberId));
        assertEquals("No Member found with ID: 1", exception.getMessage());
    }
    @Test
    void testDeleteMember_Id_Null() {
        String memberId = null;
        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.deleteMember(memberId));
        assertEquals("Member Id cannot be null", exception.getMessage());
    }

    @Test
    void testDeleteMember_CannotBeDeleted() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("test@example.com");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(new User()));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.deleteMember(memberId));
        assertEquals("Member cannot be deleted as user is already created", exception.getMessage());
    }
    @Test
    void testDeleteMember_CannotBeDeleted_PENDING() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("test@example.com");
        member.setStatus(MemberStatus.PENDING);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(userRepository.findByEmail(member.getEmail())).thenReturn(Optional.of(new User()));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.deleteMember(memberId));
        assertEquals("Member cannot be deleted as user is already created", exception.getMessage());
    }

    @Test
    void testChangeMemberStatus_Successful() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.PENDING);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtUtils.getEmail()).thenReturn("approver@example.com");

        memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of(UserRole.VISITOR));

        assertEquals(MemberStatus.APPROVED, member.getStatus());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testChangeMemberStatus_NotFound() {
        String memberId = "1";

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of(UserRole.VISITOR)));
        assertEquals("No Member found with ID: 1", exception.getMessage());
    }

    @Test
    void testChangeMemberStatus_AlreadySet() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.APPROVED);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of(UserRole.VISITOR)));
        assertEquals("Member is already APPROVED", exception.getMessage());
    }

    @Test
    void testChangeMemberStatus_DeclinedCannotBeApproved() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.DECLINED);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of(UserRole.VISITOR)));
        assertEquals("Member is already declined, delete member and register", exception.getMessage());
    }

    @Test
    void testChangeMemberStatus_ApprovedRequiresRoles() {
        String memberId = "1";
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.PENDING);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of()));
        assertEquals("At least one role is mandatory", exception.getMessage());

        exception = assertThrows(SinkValidationException.class, () -> memberService.changeMemberStatus(memberId, MemberStatus.APPROVED, Set.of(UserRole.ADMIN)));
        assertEquals("VISITOR role is mandatory", exception.getMessage());
    }
    @Test
    void testRegisterMember_Successful() {
        MemberReqDTO memberReqDTO = new MemberReqDTO(null, "test@example.com", "password", "Test User", "password", null);
        Member member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("encodedPassword");
        member.setCreatedBy("test@example.com");
        member.setCreatedTime(LocalDateTime.now());
        member.setStatus(MemberStatus.PENDING);

        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        memberService.registerMember(memberReqDTO);

        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testRegisterMember_DuplicateKeyException() {
        MemberReqDTO memberReqDTO = new MemberReqDTO(null, "test@example.com", "password", "Test User", "password", null);
        Member member = new Member();
        member.setEmail("test@example.com");
        member.setPassword("encodedPassword");
        member.setCreatedBy("test@example.com");
        member.setCreatedTime(LocalDateTime.now());
        member.setStatus(MemberStatus.PENDING);

        when(convertor.convert(any(MemberReqDTO.class), eq(Member.class))).thenReturn(member);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        doThrow(new DuplicateKeyException("Duplicate key error")).when(memberRepository).save(any(Member.class));

        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> memberService.registerMember(memberReqDTO));
        assertEquals("Duplicate key error", exception.getMessage());
    }
    @Test
    void testGetAllMembers_Successful() {
        Member member = new Member();
        member.setId("1");
        member.setEmail("test@example.com");
        member.setName("Test User");

        MemberDTO memberDTO = new MemberDTO("1", "Test User","test@example.com",  "1231231231", null, null, null, null, null, null, null);

        when(memberRepository.findAll()).thenReturn(List.of(member));
        when(convertor.convert(member, MemberDTO.class)).thenReturn(memberDTO);

        List<MemberDTO> result = memberService.getAllMembers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).id());
        assertEquals("test@example.com", result.get(0).email());
        assertEquals("Test User", result.get(0).name());
    }

    @Test
    void testGetAllMembers_EmptyList() {
        when(memberRepository.findAll()).thenReturn(Collections.emptyList());

        List<MemberDTO> result = memberService.getAllMembers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testResetPassword_Successful() {
        String memberId = "1";
        String newPassword = "newPassword";
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO(memberId, newPassword);
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.PENDING);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        memberService.resetPassword(resetPasswordReqDTO);

        verify(memberRepository, times(1)).save(member);
        assertEquals("encodedPassword", member.getPassword());
    }

    @Test
    void testResetPassword_MemberNotFound() {
        String memberId = "1";
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO(memberId, "newPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> memberService.resetPassword(resetPasswordReqDTO));
        assertEquals("No Member found with ID: 1", exception.getMessage());
    }

    @Test
    void testResetPassword_ApprovedMemberCannotResetPassword() {
        String memberId = "1";
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO(memberId, "newPassword");
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.APPROVED);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.resetPassword(resetPasswordReqDTO));
        assertEquals("Member is already approved, cannot reset password", exception.getMessage());
    }

    @Test
    void testResetPassword_DeclinedMemberCannotResetPassword() {
        String memberId = "1";
        ResetPasswordReqDTO resetPasswordReqDTO = new ResetPasswordReqDTO(memberId, "newPassword");
        Member member = new Member();
        member.setId(memberId);
        member.setStatus(MemberStatus.DECLINED);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        SinkValidationException exception = assertThrows(SinkValidationException.class, () -> memberService.resetPassword(resetPasswordReqDTO));
        assertEquals("Member is already declined, delete member and register", exception.getMessage());
    }
}