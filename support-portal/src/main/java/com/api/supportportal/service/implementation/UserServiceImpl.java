package com.api.supportportal.service.implementation;

import com.api.supportportal.domain.User;
import com.api.supportportal.domain.UserPrincipal;
import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.domain.UserNotFoundException;
import com.api.supportportal.exception.domain.UsernameExistException;
import com.api.supportportal.repository.UserRepository;
import com.api.supportportal.service.LoginAttemptService;
import com.api.supportportal.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.api.supportportal.constant.UserImplConstant.*;
import static com.api.supportportal.enumaration.Role.*;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private LoginAttemptService loginAttemptService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findUserByUsername(username);
        if (user == null){
            logger.error(USER_NOT_FOUND_BY_USERNAME + username);
            throw new UsernameNotFoundException(USER_NOT_FOUND_BY_USERNAME);
        }else {
            validadeLloginAttempt(user);
             user.setLastLoginDateDisplay(user.getLastLoginDate());
             user.setLastLoginDate(new Date());
             userRepository.save(user);
             UserPrincipal userPrincipal = new UserPrincipal(user);
             logger.info(RETURNING_FOUND_USER_BY_USERNAME + username);
             return userPrincipal;
        }
    }

    private void validadeLloginAttempt(User user){
        if(user.isNotLocked()){
            if(loginAttemptService.hasExcodeMaxAttempts(user.getUsername())){
                user.setNotLocked(false);
            }else{
                user.setActive(true);
            }
        }else{
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());
        }
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
            throws UserNotFoundException, EmailExistException, UsernameExistException {

        User userByNewUsername = findByUsername(newUsername);
        User userByNewEmail = findUserByEmail(newEmail);

        if(StringUtils.isNotBlank(currentUsername)){

            User currentUser = findByUsername(currentUsername);

            if(currentUser == null)
                throw new UserNotFoundException("No user found by username" + currentUsername);
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId()))
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId()))
                throw new EmailExistException(USERNAME_ALREADY_EXISTS);

            return currentUser;
        } else {

            if(userByNewUsername != null)
                throw new UserNotFoundException(USERNAME_ALREADY_EXISTS);
            if(userByNewEmail != null)
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);

            return null;
        }
    }

    @Override
    public User register(String fistName, String lastName, String username, String email)
            throws UserNotFoundException, EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, username, email);

        User user = new User();
        user.setUserId(generatedUserid());
        String password = generatePassword();
        String encodePassword = encodePassword(password);

        user.setFistName(fistName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        user.setJoinDate(new Date());
        user.setPassword(encodePassword);
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(ROLE_USER.name());
        user.setAuthorities(ROLE_USER.getAuthorities());
        user.setProfileImageUrl(getTemporaryProfileImageUrl());

        userRepository.save(user);
        logger.info("New user password: " + password);
        return user;
    }

    private String getTemporaryProfileImageUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PROFILE).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }

    private String generatedUserid() {
        return RandomStringUtils.randomNumeric(10);
    }


    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email);
    }
}
