package edu.cit.sabornido.rentease.controller;

import edu.cit.sabornido.rentease.dto.ApiResponse;
import edu.cit.sabornido.rentease.dto.auth.AuthRequest;
import edu.cit.sabornido.rentease.dto.auth.AuthResponse;
import edu.cit.sabornido.rentease.dto.auth.RegisterRequest;
import edu.cit.sabornido.rentease.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse res = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(res));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse res = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(ApiResponse.success(res));
    }
}
