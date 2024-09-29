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
import com.kitchen.sink.exception.ValidationException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.service.MemberService;
import com.kitchen.sink.utils.JWTUtils;
import com.kitchen.sink.utils.ObjectConvertor;
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
    private ObjectConvertor convertor;
    @Autowired
    private JWTUtils JWTUtils;
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
        String createBy = JWTUtils.getEmail();
        member.setCreatedBy(createBy);
        member.setCreatedTime(LocalDateTime.now());
        if (member.getStatus() == null) {
            member.setStatus(MemberStatus.PENDING);
        }
        memberRepository.save(member);
        log.info("Member saved: {}", member);
        return convertor.convert(member, MemberResDTO.class);
    }

    @Override
    public MemberResDTO getMember(String id) {
        log.info("Getting Member with ID: {}", id);
        if (StringUtils.isBlank(id)) {
            throw new ValidationException("Member Id cannot be null", HttpStatus.BAD_REQUEST);
        }
        Optional<Member> member = memberRepository.findById(id);
        if (member.isEmpty()) {
            throw new NotFoundException("No Member found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        log.info("Member found: {}", member.get());
        return convertor.convert(member.get(), MemberResDTO.class);
    }

    @Override
    @Transactional
    public MemberResDTO updateMember(MemberReqDTO memberReqDTO) {
        Member existingMember = memberRepository.findById(memberReqDTO.id()).
                orElseThrow(() -> new NotFoundException("No Member found with ID: " + memberReqDTO.id(), HttpStatus.NOT_FOUND));
       if(!existingMember.getStatus().equals(MemberStatus.PENDING)){
              throw new ValidationException("Member is already approved/declined, cannot update", HttpStatus.CONFLICT);
        }
        Member member = convertor.convert(memberReqDTO, Member.class);
        String updatedBy = JWTUtils.getEmail();
        member.setLastModifiedBy(updatedBy);
        member.setLastModifiedTime(LocalDateTime.now());
        if (member.getStatus() == null) {
            member.setStatus(existingMember.getStatus());
        }
        member.setCreatedBy(existingMember.getCreatedBy());
        member.setCreatedTime(existingMember.getCreatedTime());
        member.setApprovedBy(existingMember.getApprovedBy());
        member.setApprovedTime(existingMember.getApprovedTime());
        member.setPassword(existingMember.getPassword());
        return convertor.convert(memberRepository.save(member), MemberResDTO.class);
    }

    @Override
    public void deleteMember(String id) {
        if (StringUtils.isBlank(id)) {
            throw new ValidationException("Member Id cannot be null", HttpStatus.BAD_REQUEST);
        }
        Member member = memberRepository.findById(id).
                orElseThrow(() -> new NotFoundException("No Member found with ID: " + id, HttpStatus.NOT_FOUND));
        if (userRepository.findByEmail(member.getEmail()).isPresent()) {
            throw new ValidationException("Member cannot be deleted as user is already created", HttpStatus.PRECONDITION_FAILED);
        }
        memberRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changeMemberStatus(String memberId, MemberStatus status, Set<UserRole> userRoles) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new NotFoundException("No Member found with ID: " + memberId, HttpStatus.NOT_FOUND));
        if (member.getStatus() != null) {
            if (status.equals(member.getStatus())) {
                throw new ValidationException("Member is already " + status, HttpStatus.NO_CONTENT);
            }

            if (member.getStatus().equals(MemberStatus.DECLINED) && status.equals(MemberStatus.APPROVED)) {
                throw new ValidationException("Member is already declined,delete member and register", HttpStatus.CONFLICT);
            }
        }
        if (status.equals(MemberStatus.APPROVED)) {
            if (userRoles == null || userRoles.isEmpty()) {
                throw new ValidationException("At least one role is mandatory ", HttpStatus.BAD_REQUEST);
            }
            if (!userRoles.contains(UserRole.VISITOR)) {
                throw new ValidationException("VISITOR role is mandatory", HttpStatus.BAD_REQUEST);
            }
        }
        member.setStatus(status);
        member.setApprovedTime(LocalDateTime.now());
        member.setApprovedBy(JWTUtils.getEmail());
        memberRepository.save(member);
        if (status.equals(MemberStatus.APPROVED)) {
            User user = createNewUser(userRoles, member);
            userRepository.save(user);
        }
    }

    private  User createNewUser(Set<UserRole> userRoles, Member member) {
        User user = new User();
        user.setEmail(member.getEmail());
        user.setName(member.getName());
        user.setPassword(member.getPassword());
        user.setPhoneNumber(member.getPhoneNumber());
        if (userRoles != null && !userRoles.isEmpty()) {
            user.setRoles(userRoles);
        } else {
            user.setRoles(Set.of(UserRole.VISITOR));
        }
        user.setStatus(UserStatus.ACTIVE);

        user.setCreatedBy(member.getCreatedBy());
        user.setCreatedTime(member.getCreatedTime());
        user.setLastModifiedBy(JWTUtils.getEmail());
        user.setLastModifiedTime(LocalDateTime.now());
        return user;
    }

    @Override
    @Transactional
    public void registerMember(MemberReqDTO memberReqDTO) {
        Member member = convertor.convert(memberReqDTO, Member.class);
        member.setCreatedBy(member.getEmail());
        member.setCreatedTime(LocalDateTime.now());
        member.setStatus(MemberStatus.PENDING);
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
    }

    @Override
    public List<MemberDTO> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(member -> convertor.convert(member, MemberDTO.class)).toList();
    }

    @Override
    public void resetPassword(ResetPasswordReqDTO resetPasswordReqDTO) {
        Member member = memberRepository.findById(resetPasswordReqDTO.id()).
                orElseThrow(() -> new NotFoundException("No Member found with ID: " + resetPasswordReqDTO.id(), HttpStatus.NOT_FOUND));
       if (member.getStatus().equals(MemberStatus.APPROVED)) {
            throw new ValidationException("Member is already approved, cannot reset password", HttpStatus.CONFLICT);
        }
       if (member.getStatus().equals(MemberStatus.DECLINED)) {
            throw new ValidationException("Member is already declined,delete member and register", HttpStatus.CONFLICT);
        }
        member.setPassword(passwordEncoder.encode(resetPasswordReqDTO.password()));
        memberRepository.save(member);
    }


}
