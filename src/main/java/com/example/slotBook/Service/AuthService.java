package com.example.slotBook.Service;

import com.example.slotBook.Security.JwtAuthenticationFilter;
import com.example.slotBook.Security.UserDetailsImpl;
import com.example.slotBook.dto.LoginRequest;
import com.example.slotBook.dto.LoginResponse;
import com.example.slotBook.entity.AuthProvider;
import com.example.slotBook.entity.Role;
import com.example.slotBook.entity.User;
import com.example.slotBook.Repository.UserRepository;
import com.example.slotBook.dto.UserDto;
import com.example.slotBook.exception.ConflictException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager=authenticationManager;
        this.jwtService = jwtService;
    }
    public UserDto register(String email, String password, String name){
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()){
            throw new ConflictException("User with this email "+ email +" already exists");
        }
        User newUser = new User();
        String hashedPassword=passwordEncoder.encode(password);
        newUser.setPassword(hashedPassword);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setRole(Role.USER);
        newUser.setAuthProvider(AuthProvider.LOCAL);
        User savedUser =userRepository.save(newUser);
        UserDto userDto =new UserDto(savedUser.getId(),savedUser.getEmail(),savedUser.getName(),savedUser.getRole());

        return userDto ;
    }
    public LoginResponse login(LoginRequest request){
        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),request.getPassword()
                ));
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        String token=jwtService.generateToken(userDetails);
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setToken(token);
        response.setRole(user.getRole().name());
        return response;
    }


}
