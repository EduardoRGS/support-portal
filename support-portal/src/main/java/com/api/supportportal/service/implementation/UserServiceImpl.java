package com.api.supportportal.service.implementation;

import com.api.supportportal.domain.User;
import com.api.supportportal.domain.UserPrincipal;
import com.api.supportportal.enumaration.Role;
import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.domain.UserNotFoundException;
import com.api.supportportal.exception.domain.UsernameExistException;
import com.api.supportportal.repository.UserRepository;
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

import static com.api.supportportal.enumaration.Role.ROLE_USER;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findUserByUsername(username);

        if (user == null){
            logger.error("User not found by username: " + username);
            throw new UsernameNotFoundException("User not found by username: " + username);
        }else {
             user.setLastLoginDateDisplay(user.getLastLoginDate());
             user.setLastLoginDate(new Date());
             userRepository.save(user);
             UserPrincipal userPrincipal = new UserPrincipal(user);
             logger.info("Returning found user by username: " + username);
             return userPrincipal;
        }
    }

    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail)
            throws UserNotFoundException, EmailExistException, UsernameExistException {
        if(StringUtils.isNotBlank(currentUsername)){
            User currentUser = findByUsername(currentUsername);
            if(currentUser == null){
                throw new UserNotFoundException("No user found by username" + currentUsername);
            }
            User userByNewUsername = findByUsername(newUsername);
            if(userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId())){
                throw new UsernameExistException("Username already exists");
            }
            User userByNewEmail = findUserByEmail(newEmail);
            if(userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId())){
                throw new EmailExistException("Email already exists");
            }
            return currentUser;
        } else {
            User userByUsername = findByUsername(newUsername);
            if(userByUsername != null){
                throw new UserNotFoundException("No user found by username" + currentUsername);
            }
            User userByEmail = findUserByEmail(newEmail);
            if(userByEmail != null){
                throw new EmailExistException("Email already exists");
            }
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
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("user/image/profile").toUriString();
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
        return null;
    }

    @Override
    public User findByUsername(String username) {
        return null;
    }

    @Override
    public User findUserByEmail(String email) {
        return null;
    }
}
