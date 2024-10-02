package com.kitchen.sink.service.impl;

import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.ResetPasswordReqDTO;
import com.kitchen.sink.dto.response.MemberDTO;
import com.kitchen.sink.dto.response.MemberResDTO;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.enums.MemberStatus;
import com.kitchen.sink.enums.UserRole;
import com.kitchen.sink.enums.UserStatus;
import com.kitchen.sink.exception.NotFoundException;
import com.kitchen.sink.exception.SinkValidationException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.MemberService;
import com.kitchen.sink.utils.JWTUtils;
import com.kitchen.sink.utils.UniversalConverter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private UniversalConverter convertor;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public MemberResDTO saveMember(MemberReqDTO memberReqDTO) {
        log.info("Saving Member: {}", memberReqDTO);
        Member member = convertor.convert(memberReqDTO, Member.class);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        setAuditFieldsForCreate(member);
        member.setStatus(Optional.ofNullable(member.getStatus()).orElse(MemberStatus.PENDING));
        memberRepository.save(member);
        log.info("Member saved: {}", member);
        return convertor.convert(member, MemberResDTO.class);
    }

    @Override
    public MemberResDTO getMember(String id) {
        log.info("Getting Member with ID: {}", id);
        validateId(id);
        Member member = findMemberById(id);
        log.info("Member found: {}", member);
        return convertor.convert(member, MemberResDTO.class);
    }

    @Override
    @Transactional
    public MemberResDTO updateMember(MemberReqDTO memberReqDTO) {
        log.info("Updating Member with ID: {}", memberReqDTO.id());
        Member existingMember = findMemberById(memberReqDTO.id());
        validateMemberStatusForUpdate(existingMember);
        Member member = convertor.convert(memberReqDTO, Member.class);
        setAuditFieldsForUpdate(member, existingMember);
        member.setStatus(MemberStatus.PENDING);
        memberRepository.save(member);
        log.info("Member updated: {}", member);
        return convertor.convert(member, MemberResDTO.class);
    }

    @Override
    public void deleteMember(String id) {
        log.info("Deleting Member with ID: {}", id);
        validateId(id);
        Member member = findMemberById(id);
        validateMemberForDeletion(member);
        memberRepository.deleteById(id);
        log.info("Member deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public void changeMemberStatus(String memberId, MemberStatus status, Set<UserRole> userRoles) {
        log.info("Changing status of Member with ID: {} to {}", memberId, status);
        Member member = findMemberById(memberId);
        validateMemberStatusChange(member, status, userRoles);
        member.setStatus(status);
        member.setApprovedTime(LocalDateTime.now());
        member.setApprovedBy(jwtUtils.getEmail());
        memberRepository.save(member);
        User user = createNewUser(userRoles, member);
        userRepository.save(user);
        log.info("Status of Member with ID: {} changed to {}", memberId, status);
    }

    @Override
    @Transactional
    public void registerMember(MemberReqDTO memberReqDTO) {
        log.info("Registering Member: {}", memberReqDTO);
        Member member = convertor.convert(memberReqDTO, Member.class);
        setAuditFieldsForCreate(member);
        member.setStatus(MemberStatus.PENDING);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
        log.info("Member registered: {}", member);
    }

    @Override
    public List<MemberDTO> getAllMembers() {
        log.info("Fetching all Members");
        List<Member> members = memberRepository.findAll();
        List<MemberDTO> memberDTOs = members.stream().map(member -> convertor.convert(member, MemberDTO.class)).toList();
        log.info("All Members fetched");
        return memberDTOs;
    }

    @Override
    public void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO) {
        log.info("Resetting password for Member with ID: {}", resetPasswordReqDTO.id());
        Member member = findMemberById(resetPasswordReqDTO.id());
        validateMemberStatusForPasswordReset(member);
        member.setPassword(passwordEncoder.encode(resetPasswordReqDTO.password()));
        memberRepository.save(member);
        log.info("Password reset for Member with ID: {}", resetPasswordReqDTO.id());
    }

    private void validateId(String id) {
        log.debug("Validating ID: {}", id);
        if (StringUtils.isBlank(id)) {
            throw new SinkValidationException("Member Id cannot be null", HttpStatus.BAD_REQUEST);
        }
    }

    private Member findMemberById(String id) {
        log.debug("Finding Member by ID: {}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No Member found with ID: " + id, HttpStatus.NOT_FOUND));
    }

    private void validateMemberStatusForUpdate(Member existingMember) {
        log.debug("Validating Member status for update: {}", existingMember.getId());
        if (!existingMember.getStatus().equals(MemberStatus.PENDING)) {
            throw new SinkValidationException("Member is already approved/declined, cannot update", HttpStatus.CONFLICT);
        }
    }

    private void validateMemberForDeletion(Member member) {
        log.debug("Validating Member for deletion: {}", member.getId());
        if ((member.getStatus() != null && member.getStatus().equals(MemberStatus.APPROVED)) || userRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new SinkValidationException("Member cannot be deleted as user is already created", HttpStatus.PRECONDITION_FAILED);
        }
    }

    private void validateMemberStatusChange(Member member, MemberStatus status, Set<UserRole> userRoles) {
        log.debug("Validating Member status change for Member ID: {}", member.getId());
        if (status.equals(member.getStatus())) {
            throw new SinkValidationException("Member is already " + status, HttpStatus.NO_CONTENT);
        }
        if (member.getStatus().equals(MemberStatus.DECLINED)) {
            throw new SinkValidationException("Member is already declined, delete member and register", HttpStatus.CONFLICT);
        }
        if (status.equals(MemberStatus.APPROVED)) {
            if (userRoles == null || userRoles.isEmpty()) {
                throw new SinkValidationException("At least one role is mandatory", HttpStatus.BAD_REQUEST);
            }
            if (!userRoles.contains(UserRole.VISITOR)) {
                throw new SinkValidationException("VISITOR role is mandatory", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validateMemberStatusForPasswordReset(Member member) {
        log.debug("Validating Member status for password reset: {}", member.getId());
        if (member.getStatus().equals(MemberStatus.APPROVED)) {
            throw new SinkValidationException("Member is already approved, cannot reset password", HttpStatus.CONFLICT);
        }
        if (member.getStatus().equals(MemberStatus.DECLINED)) {
            throw new SinkValidationException("Member is already declined, delete member and register", HttpStatus.CONFLICT);
        }
    }

    private void setAuditFieldsForCreate(Member member) {
        log.debug("Setting audit fields for create for Member ID: {}", member.getId());
        String createdBy = jwtUtils.getEmail();
        member.setCreatedBy(createdBy);
        member.setCreatedTime(LocalDateTime.now());
    }

    private void setAuditFieldsForUpdate(Member member, Member existingMember) {
        log.debug("Setting audit fields for update for Member ID: {}", member.getId());
        String updatedBy = jwtUtils.getEmail();
        member.setLastModifiedBy(updatedBy);
        member.setLastModifiedTime(LocalDateTime.now());
        member.setCreatedBy(existingMember.getCreatedBy());
        member.setCreatedTime(existingMember.getCreatedTime());
        member.setApprovedBy(existingMember.getApprovedBy());
        member.setApprovedTime(existingMember.getApprovedTime());
        member.setPassword(existingMember.getPassword());
    }

    private User createNewUser(Set<UserRole> userRoles, Member member) {
        log.debug("Creating new User for Member ID: {}", member.getId());
        User user = new User();
        user.setEmail(member.getEmail());
        user.setName(member.getName());
        user.setPassword(member.getPassword());
        user.setPhoneNumber(member.getPhoneNumber());
        user.setRoles(Optional.ofNullable(userRoles).orElse(Set.of(UserRole.VISITOR)));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedBy(member.getCreatedBy());
        user.setCreatedTime(member.getCreatedTime());
        user.setLastModifiedBy(jwtUtils.getEmail());
        user.setLastModifiedTime(LocalDateTime.now());
        return user;
    }
}