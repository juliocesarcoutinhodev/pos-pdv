package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.RegisterUserUseCase;
import br.com.topone.backend.application.usecase.UseCaseResponseMapper;
import br.com.topone.backend.interfaces.dto.AuthResponse;
import br.com.topone.backend.interfaces.dto.DtoCommandMapper;
import br.com.topone.backend.interfaces.dto.RegisterRequest;
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
    private final DtoCommandMapper dtoCommandMapper;
    private final UseCaseResponseMapper responseMapper;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var command = dtoCommandMapper.toCommand(request);
        var result = registerUserUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(result));
    }

    // TODO: login, refresh endpoints
}
