package com.example.auth_backend.controller;

import com.example.auth_backend.dto.LoginRequestDTO;
import com.example.auth_backend.dto.LoginResponseDTO;
import com.example.auth_backend.dto.UserCreateDTO;
import com.example.auth_backend.dto.UserDTO;
import com.example.auth_backend.entity.RefreshToken;
import com.example.auth_backend.service.AuthService;
import com.example.auth_backend.service.JwtService;
import com.example.auth_backend.service.RefreshTokenService;
import com.example.auth_backend.mapper.UserMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AuthController {
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days in seconds

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    // helper to set (or clear) the refresh token cookie. If token is null or empty,
    // cookie will be cleared (Max-Age=0).
    private void setRefreshCookie(HttpServletResponse response, String token) {
        long maxAge = (token == null || token.trim().isEmpty()) ? 0 : REFRESH_COOKIE_MAX_AGE;
        String value = token == null ? "" : token;
            // If cookies are marked Secure (production over HTTPS), browsers require
            // SameSite=None for cross-site cookies. For non-secure cookies use Lax.
            String sameSite = secureCookie ? "None" : "Lax";

            ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // helper to fetch a cookie value by name from the request. Returns null when
    // cookie not found or empty.
    private String getCookie(HttpServletRequest req, String name) {
        if (req == null || name == null)
            return null;
        jakarta.servlet.http.Cookie[] cookies = req.getCookies();
        if (cookies == null)
            return null;
        for (jakarta.servlet.http.Cookie c : cookies) {
            if (name.equals(c.getName())) {
                String v = c.getValue();
                if (v == null || v.trim().isEmpty())
                    return null;
                return v;
            }
        }
        return null;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserCreateDTO userCreateDTO) {
        UserDTO created = authService.registerUser(userCreateDTO);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse response) {
        // authenticate and get access token + user (service also produces a raw refresh
        // token but it is JsonIgnored)
        LoginResponseDTO loginResp = authService.loginUser(loginRequestDTO);

        // if service provided a refresh token raw, set it as httpOnly cookie
        if (loginResp.getRefreshTokenRaw() != null) {
            setRefreshCookie(response, loginResp.getRefreshTokenRaw());
        }

        return ResponseEntity.ok(loginResp);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request, HttpServletResponse response) {
        String raw = getCookie(request, REFRESH_COOKIE_NAME);
        if (raw == null) {
            return ResponseEntity.status(401).build();
        }

        // validate existing refresh token and obtain user
        RefreshToken token = refreshTokenService.validateRefreshToken(raw);
        // rotate: revoke old and create a new one (raw value)
        String newRaw = refreshTokenService.rotateRefreshToken(raw);

        // create new access token for the user
        String newAccess = jwtService.generateAccessToken(token.getUser());

        // set new cookie
        setRefreshCookie(response, newRaw);

        LoginResponseDTO resp = new LoginResponseDTO();
        resp.setToken(newAccess);
        resp.setUser(UserMapper.mapToUserDTO(token.getUser()));
        resp.setRefreshTokenRaw(newRaw);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String raw = getCookie(request, REFRESH_COOKIE_NAME);
        if (raw != null) {
            try {
                refreshTokenService.revokeRefreshToken(raw);
            } catch (Exception ignored) {
            }
        }

        // clear cookie regardless
        setRefreshCookie(response, null);

        return ResponseEntity.ok().build();
    }
}
