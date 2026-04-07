package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.EmailAlreadyExistsException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.repository.RoleRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase useCase;

    private RegisterUserCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterUserCommand("user@test.com", "Test User", "password123");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.resolveByNames(Set.of("USER"))).thenReturn(Set.of(Role.create("USER", "Usuário padrão")));
        when(passwordEncoder.encode(command.password())).thenReturn("hashedPassword");

        var savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(command.email());
        savedUser.setName(command.name());
        savedUser.setPasswordHash("hashedPassword");
        savedUser.setProvider(AuthProvider.LOCAL);
        savedUser.setCreatedAt(Instant.now());
        savedUser.setUpdatedAt(Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var result = useCase.execute(command);

        assertThat(result.id()).isEqualTo(savedUser.getId());
        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.provider()).isEqualTo("LOCAL");
    }

    @Test
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already registered");

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.resolveByNames(Set.of("USER"))).thenReturn(Set.of(Role.create("USER", "Usuário padrão")));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        var savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("user@test.com");
        savedUser.setName("Test User");
        savedUser.setPasswordHash("encoded");
        savedUser.setProvider(AuthProvider.LOCAL);
        savedUser.setCreatedAt(Instant.now());
        savedUser.setUpdatedAt(Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        useCase.execute(command);

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldNotExposePasswordInResult() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.resolveByNames(Set.of("USER"))).thenReturn(Set.of(Role.create("USER", "Usuário padrão")));
        when(passwordEncoder.encode(command.password())).thenReturn("hashedPassword");

        var savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("user@test.com");
        savedUser.setName("Test User");
        savedUser.setPasswordHash("hashedPassword");
        savedUser.setProvider(AuthProvider.LOCAL);
        savedUser.setCreatedAt(Instant.now());
        savedUser.setUpdatedAt(Instant.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var result = useCase.execute(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.provider()).isEqualTo("LOCAL");
    }

    @Test
    void shouldCreateUserAsLocalProvider() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(roleRepository.resolveByNames(Set.of("USER"))).thenReturn(Set.of(Role.create("USER", "Usuário padrão")));
        when(passwordEncoder.encode(command.password())).thenReturn("hashedPassword");

        var userCaptor = ArgumentCaptor.forClass(User.class);
        var savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail("user@test.com");
        savedUser.setName("Test User");
        savedUser.setPasswordHash("hashedPassword");
        savedUser.setProvider(AuthProvider.LOCAL);
        savedUser.setCreatedAt(Instant.now());
        savedUser.setUpdatedAt(Instant.now());
        when(userRepository.save(userCaptor.capture())).thenReturn(savedUser);

        useCase.execute(command);

        var capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }
}
