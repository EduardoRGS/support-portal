package com.api.supportportal.resource;

import com.api.supportportal.exception.domain.EmailExistException;
import com.api.supportportal.exception.ExceptionHandling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = {"/","/user"})
public class UserResource extends ExceptionHandling {

    @GetMapping("/home")
    public String showUser() throws EmailExistException{
        throw new EmailExistException(("This email address is already taken"));
      // return "application works";
   }
}
