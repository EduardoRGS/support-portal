package com.api.supportportal.resource;

import com.api.supportportal.domain.HttpResponse;
import com.api.supportportal.domain.User;
import com.api.supportportal.domain.UserPrincipal;
import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.ExceptionHandling;
import com.api.supportportal.exception.domain.EmailNotFoundException;
import com.api.supportportal.exception.domain.UserNotFoundException;
import com.api.supportportal.exception.domain.UsernameExistException;
import com.api.supportportal.service.UserService;
import com.api.supportportal.utility.JWTTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.api.supportportal.constant.FileConstant.*;
import static com.api.supportportal.constant.SecurityConstant.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserResource extends ExceptionHandling {

    public static final String EMAIL_SENT = "An Email with a new password was sent to: ";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
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
        return new ResponseEntity<>(loginUser, jwtHeader, OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user)
            throws UserNotFoundException, EmailExistException, UsernameExistException, MessagingException {
        User newUser = userService.register(user.getFistName(), user.getLastName(), user.getUsername(), user.getEmail());
        return new ResponseEntity<>(newUser, OK);
   }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(@RequestParam("fistName") String fistName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam( value = "profileImage", required = false) MultipartFile profileImage)
           throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User newUser = userService.addNewUser(fistName, lastName, username, email, role,
                Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNonLocked), profileImage);
        return new ResponseEntity<>(newUser, OK);
   }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestParam("currentUsername") String currentUsername,
                                           @RequestParam("fistName") String fistName,
                                           @RequestParam("lastName") String lastName,
                                           @RequestParam("username") String username,
                                           @RequestParam("email") String email,
                                           @RequestParam("role") String role,
                                           @RequestParam("isActive") String isActive,
                                           @RequestParam("isNonLocked") String isNonLocked,
                                           @RequestParam( value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User updateUser = userService.updateUser(currentUsername, fistName, lastName, username, email, role,
                Boolean.parseBoolean(isActive), Boolean.parseBoolean(isNonLocked), profileImage);
        return new ResponseEntity<>(updateUser, OK);
    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username){
        User user = userService.findByUsername(username);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, OK);
    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email)
            throws EmailNotFoundException, MessagingException {
        userService.resetPassword(email);
        return response(OK, EMAIL_SENT + email);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") long id){
        userService.deleteUser(id);
        return response(NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PutMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage (@RequestParam("username") String username,
                                                    @RequestParam( value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, EmailExistException, IOException, UsernameExistException {
        User user = userService.updateProfileImage(username, profileImage);
        return new ResponseEntity<>(user, OK);
    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try(InputStream inputStream = url.openStream()){
            int bytesRead;
            byte[] part = new byte[1024];
            while ((bytesRead = inputStream.read(part)) > 0){
                byteArrayOutputStream.write(part, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {
        return new ResponseEntity<>(new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase()), httpStatus);
    }

    private HttpHeaders getJwtHeaders(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
