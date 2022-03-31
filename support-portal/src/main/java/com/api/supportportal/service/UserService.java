package com.api.supportportal.service;

import com.api.supportportal.domain.User;
import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.domain.UserNotFoundException;
import com.api.supportportal.exception.domain.UsernameExistException;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {

    User register(String fistName, String lastName, String username, String email) throws UserNotFoundException, EmailExistException, UsernameExistException;

    List<User> getAllUsers();

    User findByUsername(String username);

    User findUserByEmail(String email);
}
