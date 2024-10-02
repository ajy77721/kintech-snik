package com.kitchen.sink.aspect;

import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.UserReqDTO;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.exception.UniqueEmailException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.utils.JWTUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

import com.kitchen.sink.dto.request.MemberReqDTO;
import com.kitchen.sink.dto.request.UserReqDTO;
import com.kitchen.sink.entity.Member;
import com.kitchen.sink.entity.User;
import com.kitchen.sink.exception.UniqueEmailException;
import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import com.kitchen.sink.utils.JWTUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, Object> {

    @Autowired
    private UserRepository userRepository; // Inject your user repository
    @Autowired
    private MemberRepository memberRepository; // Inject your member repository
    @Autowired
    private JWTUtils jwtUtils;
    private String message;

    @Override
    public void initialize(UniquePhoneNumber constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object objectDto, ConstraintValidatorContext context) {
        if (isUniquePhoneNumber(objectDto)) {
            throw new UniqueEmailException(message, HttpStatus.CONFLICT);
        }
        return true;

    }

    private boolean isUniquePhoneNumber(Object objectDto) {
        if (objectDto == null) {
            return false;
        }
        String phoneNumber = null;
        if (objectDto instanceof MemberReqDTO memberReqDTO) {
            phoneNumber = memberReqDTO.phoneNumber();
            if (memberReqDTO.id() != null) {
                Optional<Member> memberOptional = memberRepository.findById(memberReqDTO.id());
                if (memberOptional.isPresent()) {
                    Member existingMember = memberOptional.get();
                    if (existingMember.getPhoneNumber().equals(memberReqDTO.phoneNumber()))
                        return false;
                }
            }
        } else if (objectDto instanceof UserReqDTO userReqDTO) {
            phoneNumber = userReqDTO.phoneNumber();
            if (userReqDTO.id() != null) {
                Optional<User> userOptional = userRepository.findById(userReqDTO.id());
                if (userOptional.isPresent()) {
                    User existingUser = userOptional.get();
                    if (existingUser.getPhoneNumber().equals(userReqDTO.phoneNumber())) {
                        return false;
                    }
                }
            }
        }
        if (StringUtils.isBlank(phoneNumber)) {
            return true;
        }
        return (userRepository.existsByPhoneNumber(phoneNumber) || memberRepository.existsByPhoneNumber(phoneNumber));
    }
}
