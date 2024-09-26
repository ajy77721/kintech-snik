package com.kitchen.sink.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JWTConstant {
    final public static String ROLES = "roles";
    final public static String  AUTHORIZATION="Authorization";
    final public static String  BEARER="Bearer ";
    final public static String ANONYMOUS_USER = "anonymousUser";

}
