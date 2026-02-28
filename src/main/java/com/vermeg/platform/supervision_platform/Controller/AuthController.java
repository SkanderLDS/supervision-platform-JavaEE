package com.vermeg.platform.supervision_platform.Controller;
import com.vermeg.platform.supervision_platform.Config.JwtUtil;
import com.vermeg.platform.supervision_platform.DTO.LoginRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.LoginResponseDTO;
import com.vermeg.platform.supervision_platform.DTO.UserRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.UserResponseDTO;
import com.vermeg.platform.supervision_platform.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        String token = jwtUtil.generateToken(dto.getUsername());
        java.util.Set<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(LoginResponseDTO.builder().token(token).username(dto.getUsername())
                .roles(roles).build());
    }

    /* =========================
       REGISTER
       ========================= */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRequestDTO dto) {
        return ResponseEntity.ok(userService.register(dto));
    }
}
