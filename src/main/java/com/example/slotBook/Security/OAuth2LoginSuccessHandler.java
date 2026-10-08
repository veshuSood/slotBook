package com.example.slotBook.Security;

import com.example.slotBook.entity.AuthProvider;
import com.example.slotBook.entity.Role;
import com.example.slotBook.entity.User;
import com.example.slotBook.Repository.UserRepository;
import com.example.slotBook.Service.JwtService;
import com.example.slotBook.exception.ConflictException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User googleUser =
                (OAuth2User) authentication.getPrincipal();

        String email = googleUser.getAttribute("email");
        String name = googleUser.getAttribute("name");

        System.out.println("=== GOOGLE OAUTH SUCCESS HANDLER ===");
        System.out.println("Google email: " + email);
        System.out.println("Google name: " + name);

        User user = userRepository.findByEmail(email)
                .map(existingUser -> {

                    if (existingUser.getAuthProvider() != AuthProvider.GOOGLE) {
                        throw new ConflictException(
                                "An account already exists with this email. " +
                                        "Please use local login."
                        );
                    }

                    return existingUser;
                })
                .orElseGet(() -> {
                    User newUser = new User();

                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole(Role.USER);
                    newUser.setAuthProvider(AuthProvider.GOOGLE);
                    newUser.setPassword(null);

                    return userRepository.save(newUser);
                });

        System.out.println("User created/found: " + user.getId());

        String token = jwtService.generateToken(
                new UserDetailsImpl(user)
        );

        System.out.println("JWT generated successfully");
        System.out.println("Redirecting to frontend...");

        response.sendRedirect(
                "http://localhost:5173/oauth-success?token=" + token
        );
    }
}