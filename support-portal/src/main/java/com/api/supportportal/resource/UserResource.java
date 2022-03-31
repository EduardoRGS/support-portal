package com.api.supportportal.resource;

import com.api.supportportal.domain.User;
import com.api.supportportal.domain.UserPrincipal;
import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.ExceptionHandling;
import com.api.supportportal.exception.domain.UserNotFoundException;
import com.api.supportportal.exception.domain.UsernameExistException;
import com.api.supportportal.service.UserService;
import com.api.supportportal.utility.JWTTokenProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import static com.api.supportportal.constant.SecurityConstant.JWT_TOKEN_HEADER;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserResource extends ExceptionHandling {

    private UserService userService;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserResource(UserService userService, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User user)  {
        authenticate(user.getUsername(), user.getPassword());
        User loginUser =  userService.findByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeaders(userPrincipal);
        return new ResponseEntity<>(loginUser, jwtHeader, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) throws UserNotFoundException, EmailExistException, UsernameExistException {
        User newUser = userService.register(user.getFistName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
   }

    private HttpHeaders getJwtHeaders(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.genetareJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
