package com.kitchen.sink.service.impl;

import com.kitchen.sink.dto.request.MemberReqDTO;
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
import com.kitchen.sink.service.UserService;
import com.kitchen.sink.utils.JwtUtil;
import com.kitchen.sink.utils.ObjectConvertor;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
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
    private JwtUtil jwtUtil;
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
        String createBy=jwtUtil.getEmail();
        if (createBy==null){
            createBy=member.getEmail();
        }
        member.setCreatedBy(createBy);
        member.setCreatedTime(LocalDateTime.now());
        if (member.getStatus()==null){
            member.setStatus(MemberStatus.PENDING);
        }
        memberRepository.save(member);
        log.info("Member saved: {}", member);
        return convertor.convert(member, MemberResDTO.class);
    }

    @Override
    public MemberResDTO getMember(String id) {
        log.info("Getting Member with ID: {}", id);
        if (id == null) {
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
        Member member = convertor.convert(memberReqDTO, Member.class);
        String updatedBy=jwtUtil.getEmail();
        member.setUpdatedBy(updatedBy);
        member.setUpdatedTime(LocalDateTime.now());
        if (member.getStatus()==null){
            member.setStatus(existingMember.getStatus());
        }
        member.setCreatedBy(existingMember.getCreatedBy());
        member.setCreatedTime(existingMember.getCreatedTime());
        member.setApprovedBy(existingMember.getApprovedBy());
        member.setApprovedTime(existingMember.getApprovedTime());
        return convertor.convert(memberRepository.save(member), MemberResDTO.class);
    }

    @Override
    public void deleteMember(String id) {
        if (id == null) {
            throw new ValidationException("Member Id cannot be null", HttpStatus.BAD_REQUEST);
        }
        if (!memberRepository.existsById(id)) {
            throw new NotFoundException("No Member found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        memberRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void changeMemberStatus(String memberId, MemberStatus status, Set<UserRole> userRoles) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new NotFoundException("No Member found with ID: " + memberId, HttpStatus.NOT_FOUND));
        member.setStatus(status);
        member.setApprovedTime(LocalDateTime.now());
        member.setApprovedBy(jwtUtil.getEmail());
        memberRepository.save(member);
        if (status.equals(MemberStatus.APPROVED)){
            User user = createNewUser(userRoles, member);
            userRepository.save(user);
        }
    }

    private static User createNewUser(Set<UserRole> userRoles, Member member) {
        User user = new User();
        user.setEmail(member.getEmail());
        user.setName(member.getName());
        user.setPassword(member.getPassword());
        if (userRoles !=null && !userRoles.isEmpty()){
            user.setRoles(userRoles);
        }else {
            user.setRoles(Set.of(UserRole.VISITOR));
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedBy(member.getCreatedBy());
        user.setCreatedTime(member.getCreatedTime());
        return user;
    }

    @Override
    @Transactional
    public void registerMember(MemberReqDTO memberReqDTO) {
        Member member = convertor.convert(memberReqDTO, Member.class);
        member.setCreatedBy(member.getEmail());
        member.setCreatedTime(LocalDateTime.now());
        member.setStatus(MemberStatus.PENDING);
        memberRepository.save(member);
    }

    @Override
    public List<MemberDTO> getAllMembers() {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(member -> convertor.convert(member, MemberDTO.class)).toList();
    }

}
