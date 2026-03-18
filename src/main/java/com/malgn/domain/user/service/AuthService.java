package com.malgn.domain.user.service;

import com.malgn.domain.user.dto.AuthDto;
import com.malgn.domain.user.entity.User;
import com.malgn.domain.user.repository.UserRepository;
import com.malgn.global.exception.DuplicateUsernameException;
import com.malgn.global.security.JwtTokenProvider;
import com.malgn.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        // 아이디/비밀번호 검증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword())
        );

        // 토큰 발급
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(principal);

        return new AuthDto.TokenResponse(
                token,
                principal.getUsername(),
                principal.getUser().getRole().name()
        );
    }

    @Transactional
    public AuthDto.TokenResponse register(AuthDto.RegisterRequest request) {
        // 중복 아이디 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(
                    "Username already exists: " + request.getUsername());
        }

        // role 변환
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be ADMIN or USER");
        }

        // 유저 저장
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        userRepository.save(user);

        // 가입 후 바로 토큰 발급
        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(principal);

        return new AuthDto.TokenResponse(token, user.getUsername(), user.getRole().name());
    }
}