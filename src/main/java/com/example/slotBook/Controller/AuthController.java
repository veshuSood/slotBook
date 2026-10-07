package com.example.slotBook.Controller;

import com.example.slotBook.Service.AuthService;
import com.example.slotBook.dto.LoginRequest;
import com.example.slotBook.dto.LoginResponse;
import com.example.slotBook.dto.RegisterRequest;
import com.example.slotBook.dto.UserDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest.getEmail(),registerRequest.getPassword(),registerRequest.getName());
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
