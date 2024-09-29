package com.kitchen.sink.aspect;

import com.kitchen.sink.repo.MemberRepository;
import com.kitchen.sink.repo.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailExistsValidator implements ConstraintValidator<EmailExists, String> {

    @Autowired
    private UserRepository userRepository; // Inject your user repository
    @Autowired
    private MemberRepository memberRepository; // Inject your member repository

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Let other validations handle null/empty checks
        }
        return !(userRepository.existsByEmail(email) || memberRepository.existsByEmail(email));
    }
}
