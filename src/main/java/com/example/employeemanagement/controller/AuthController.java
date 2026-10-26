package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.ApiResponseDto;
import com.example.employeemanagement.dto.AuthRequestDto;
import com.example.employeemanagement.dto.AuthResponseDto;
import com.example.employeemanagement.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication and active session endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and establish session", description = "Verifies username/password and creates a session")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@Valid @RequestBody AuthRequestDto request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isAdmin = SecurityUtils.isAdmin();
        AuthResponseDto responseDto = new AuthResponseDto(
                authentication.getName(),
                authentication.getName(),
                roles,
                isAdmin,
                "Authentication successful"
        );

        return ResponseEntity.ok(ApiResponseDto.ok("Login successful", responseDto));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info", description = "Returns active user identity and roles")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponseDto.ok(new AuthResponseDto("anonymous", "Anonymous", Set.of(), false, "Not authenticated")));
        }

        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        boolean isAdmin = SecurityUtils.isAdmin();
        AuthResponseDto responseDto = new AuthResponseDto(
                authentication.getName(),
                authentication.getName(),
                roles,
                isAdmin,
                "User authenticated"
        );

        return ResponseEntity.ok(ApiResponseDto.ok(responseDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout active user", description = "Invalidates session and clears security context")
    public ResponseEntity<ApiResponseDto<String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponseDto.ok("Logged out successfully", "Session ended"));
    }
}
