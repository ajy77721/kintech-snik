package com.kitchen.sink.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JWTConstant {
    final public static String ROLES = "roles";
    final public static String  AUTHORIZATION="Authorization";
    final public static String  BEARER="Bearer ";
    final public static String ANONYMOUS_USER = "anonymousUser";
    final public static String ACCESS_DENIED ="Access Denied";
    final public static String ACCESS_DENIED_MESSAGE="You do not have permission to access this functionality. Please contact the Administrator.";
    final public static String LOGOUT_SUCCESS_MESSAGE="Logout success";
    final public static String USER_NOT_FOUND_MESSAGE="invalid user request !";
}
