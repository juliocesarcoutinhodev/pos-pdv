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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void userToken_shouldAccessUsersListEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userToken_shouldAccessRolesListEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/roles")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userToken_shouldAccessSuppliersListEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(get("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void userToken_shouldGet403OnCreateUserEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "novo@email.com",
                                  "name": "Novo Usuário",
                                  "password": "123456",
                                  "roleIds": ["00000000-0000-0000-0000-000000000001"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userToken_shouldGet403OnCreateRoleEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(post("/api/v1/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "MANAGER",
                                  "description": "Gerente"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userToken_shouldCreateSupplierEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(post("/api/v1/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Fornecedor XPTO",
                                  "taxId": "37335118000180",
                                  "email": "contato@fornecedor.com",
                                  "phone": "11999999999",
                                  "address": {
                                    "zipCode": "03195000",
                                    "street": "Rua do Oratório",
                                    "number": "100",
                                    "district": "Alto da Mooca",
                                    "city": "São Paulo",
                                    "state": "SP"
                                  }
                                 }
                                 """))
                .andExpect(status().isCreated());
    }

    @Test
    void userToken_shouldGet403OnPatchSupplierEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(patch("/api/v1/suppliers/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "phone": "11999990000"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userToken_shouldGet403OnDeactivateSupplierEndpoint() throws Exception {
        var user = buildUser(Set.of(Role.create("USER", "Usuário padrão")));
        var token = jwtTokenService.generateAccessToken(user);

        mockMvc.perform(delete("/api/v1/suppliers/" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
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
