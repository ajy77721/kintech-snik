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

@Component
@Slf4j
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, Object> {

    @Autowired
    private UserRepository userRepository; // Inject your user repository
    @Autowired
    private MemberRepository memberRepository; // Inject your member repository
    @Autowired
    private JWTUtils jwtUtils;
    private String message;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object objectDto, ConstraintValidatorContext context) {
        if (isUniqueEmail(objectDto)) {
            throw new UniqueEmailException(message, HttpStatus.CONFLICT);
        }
        return true;

    }

    private boolean isUniqueEmail(Object objectDto) {
        if (objectDto == null) {
            return false;
        }
        String email = null;
        if (objectDto instanceof MemberReqDTO memberReqDTO) {
            email = memberReqDTO.email();
            if (memberReqDTO.id() != null) {
                Optional<Member> memberOptional = memberRepository.findById(memberReqDTO.id());
                if (memberOptional.isPresent()) {
                    Member existingMember = memberOptional.get();
                    if (existingMember.getEmail().equals(memberReqDTO.email()))
                        return false;
                }
            }
        } else if (objectDto instanceof UserReqDTO userReqDTO) {
            email = userReqDTO.email();
            if (userReqDTO.id() != null) {
                Optional<User> userOptional = userRepository.findById(userReqDTO.id());
                if (userOptional.isPresent()) {
                    User existingUser = userOptional.get();
                    if (existingUser.getEmail().equals(userReqDTO.email())) {
                        return false;
                    }
                }
            }
        }
        if (StringUtils.isBlank(email)) {
            return true;
        }
        return (userRepository.existsByEmail(email) || memberRepository.existsByEmail(email));
    }
}
