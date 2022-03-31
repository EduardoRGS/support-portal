package com.api.supportportal.constant;

public class SecurityConstant {
    public static final long EXPIRATION_TIME =  432_000_000; //Tempo de expiração do token hwt 5 dias
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    public static final String GET_ARRAYS_LLC = "Get Arrays, LLc";
    public static final String GET_ARRAYS_ADMINISTRATION = "User Management Portal";
    public static final String AUTHORITIES = "authorities";
    public static final String FORBIDDEN_MESSAGE = "You need to log i to access this page";
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    public static final String OPTIONS_HTTP_METHOD = "Options";
    //public static final String[] PUBLIC_URLS =  { "/user/login", "/user/register", "/user/resetpassword/**", "/user/image/**" };
    public static final String[] PUBLIC_URLS = {"**"};
}
