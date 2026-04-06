package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.*;
import br.com.topone.backend.infrastructure.mapper.LoginResponseMapper;
import br.com.topone.backend.infrastructure.mapper.RefreshResponseMapper;
import br.com.topone.backend.infrastructure.security.RefreshTokenCookieService;
import br.com.topone.backend.interfaces.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final DtoCommandMapper dtoCommandMapper;
    private final UseCaseResponseMapper responseMapper;
    private final LoginResponseMapper loginResponseMapper;
    private final RefreshResponseMapper refreshResponseMapper;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var command = dtoCommandMapper.toCommand(request);
        var result = registerUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        var command = dtoCommandMapper.toLoginCommand(request);
        var result = loginUseCase.execute(command);
        refreshTokenCookieService.setCookie(response, result.refreshToken());
        return ResponseEntity.ok(loginResponseMapper.toResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        var refreshToken = refreshTokenCookieService.getTokenValue(request);
        var result = refreshTokenUseCase.execute(refreshToken);
        refreshTokenCookieService.setCookie(response, result.refreshToken());
        return ResponseEntity.ok(refreshResponseMapper.toResponse(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var refreshToken = refreshTokenCookieService.getTokenValue(request);
        logoutUseCase.execute(refreshToken);
        refreshTokenCookieService.clearCookie(response);
        return ResponseEntity.noContent().build();
    }
}
