package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.LoginUseCase;
import br.com.topone.backend.application.usecase.RegisterUserUseCase;
import br.com.topone.backend.application.usecase.RefreshTokenCommand;
import br.com.topone.backend.application.usecase.RefreshTokenUseCase;
import br.com.topone.backend.application.usecase.UseCaseResponseMapper;
import br.com.topone.backend.infrastructure.mapper.LoginResponseMapper;
import br.com.topone.backend.infrastructure.mapper.RefreshResponseMapper;
import br.com.topone.backend.interfaces.dto.*;
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
    private final UseCaseResponseMapper responseMapper;
    private final LoginResponseMapper loginResponseMapper;
    private final RefreshResponseMapper refreshResponseMapper;
    private final DtoCommandMapper dtoCommandMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var command = dtoCommandMapper.toCommand(request);
        var result = registerUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(result));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var command = dtoCommandMapper.toLoginCommand(request);
        var result = loginUseCase.execute(command);
        return ResponseEntity.ok(loginResponseMapper.toResponse(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        var command = dtoCommandMapper.toRefreshCommand(request);
        var result = refreshTokenUseCase.execute(command);
        return ResponseEntity.ok(refreshResponseMapper.toResponse(result));
    }
}
