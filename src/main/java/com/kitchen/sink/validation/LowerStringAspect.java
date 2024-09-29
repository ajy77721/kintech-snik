package com.kitchen.sink.validation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class LowerStringAspect {

    @Around("execution(* *(.., @LowerString (*), ..))")
    public Object convertLowerString(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Iterate over parameters
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (method.getParameters()[i].isAnnotationPresent(LowerString.class)) {
                if (args[i] instanceof String) {
                    args[i] = ((String) args[i]).toLowerCase();
                }
            }
        }

        return joinPoint.proceed(args);
    }
}
