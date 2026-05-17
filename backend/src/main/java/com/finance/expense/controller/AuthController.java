package com.finance.expense.controller;

import com.finance.expense.dto.AuthRequest;
import com.finance.expense.dto.AuthResponse;
import com.finance.expense.dto.RegisterRequest;
import com.finance.expense.entity.User;
import com.finance.expense.repository.UserRepository;
import com.finance.expense.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        String jwt = jwtUtils.generateToken(loginRequest.getEmail());
        User user = userRepository.findUserByEmail(loginRequest.getEmail()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(jwt, user.getFullName(), user.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        User user = User.builder()
                .fullName(signUpRequest.getFullName())
                .email(signUpRequest.getEmail())
                .password(encoder.encode(signUpRequest.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}
