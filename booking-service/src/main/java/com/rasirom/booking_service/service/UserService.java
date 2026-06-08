package com.rasirom.booking_service.service;

import com.rasirom.booking_service.dto.LoginRequest;
import com.rasirom.booking_service.dto.LoginResponse;
import com.rasirom.booking_service.dto.RegisterRequest;
import com.rasirom.booking_service.model.User;
import com.rasirom.booking_service.repository.UserRepository;
import com.rasirom.booking_service.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(newUser);
    }

    public LoginResponse login(LoginRequest request) {
        User existing = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), existing.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        existing.setLastLoginAt(LocalDateTime.now());
        userRepository.save(existing);

        String token = jwtUtil.generateToken(existing.getEmail());

        return new LoginResponse(token, existing.getEmail(), existing.getFirstName(), existing.getLastName());
    }
}
