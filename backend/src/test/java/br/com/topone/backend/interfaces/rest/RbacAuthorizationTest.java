package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.infrastructure.security.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class RbacAuthorizationTest {

    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void tearDown() {
        org.springframework.security.test.context.TestSecurityContextHolder.clearContext();
    }

    @Test
    void adminToken_shouldAccessAdminEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("ADMIN", "Administrador do sistema")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userToken_shouldGet403OnAdminEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/admin/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Proibido"));
    }

    @Test
    void adminToken_shouldAccessAdminOnlyEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("ADMIN", "Administrador do sistema")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/admin-only")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userToken_shouldGet403OnAdminOnlyEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/admin-only")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void userToken_shouldAccessRegularEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void adminToken_shouldAccessRegularEndpoint() throws Exception {
        var user = buildUser(Set.of(
                Role.create("ADMIN", "Administrador do sistema"),
                Role.create("USER", "Usuário padrão")
        ));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void noToken_shouldGet401OnProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    private User buildUser(Set<Role> roles) {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@email.com");
        user.setName("Test User");
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(roles);
        return user;
    }
}
