package com.api.supportportal.listener;

import com.api.supportportal.domain.User;
import com.api.supportportal.domain.UserPrincipal;
import com.api.supportportal.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class AuthenticationSuccessListener {
    private LoginAttemptService loginAttemptService;

    @Autowired
    public AuthenticationSuccessListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if(principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) event.getAuthentication().getPrincipal();
            loginAttemptService.evictUserFromLoginAttemptCache(userPrincipal.getUsername());
        }
    }
}
