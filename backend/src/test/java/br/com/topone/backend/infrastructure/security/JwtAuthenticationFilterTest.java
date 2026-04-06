package br.com.topone.backend.infrastructure.security;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.EnumSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtProperties properties;
    private JwtTokenService tokenService;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret("b2VpZG90YXdlcmVhZ29vZHNlY3JldGtleWZvcnRlc3RpbmdwdXJwb3Nlcw==");
        properties.setIssuer("pospdv");
        properties.setAccessTokenExpiration(3600);
        properties.setRefreshTokenExpiration(604800);
        properties.init();

        tokenService = new JwtTokenService(properties);
        filter = new JwtAuthenticationFilter(tokenService);
    }

    @Test
    void shouldPassThroughWithoutToken() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldAuthenticateWithValidToken() throws Exception {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setProvider(AuthProvider.LOCAL);

        var token = tokenService.generateAccessToken(user);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        var principal = (User) auth.getPrincipal();
        assertThat(principal.getEmail()).isEqualTo("user@test.com");
        assertThat(principal.getName()).isEqualTo("Test User");
        assertThat(principal.getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldReturn401WithInvalidToken() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.here");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldReturn401WithRandomGibberishToken() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-random-gibberish-token");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Token");
    }

    @Test
    void shouldPassThroughWithNonBearerHeader() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldIncludeRolesAsAuthorities() throws Exception {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@test.com");
        user.setName("Admin User");
        user.setProvider(AuthProvider.LOCAL);
        user.setRoles(EnumSet.of(Role.USER, Role.ADMIN));

        var token = tokenService.generateAccessToken(user);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).hasSize(2);
        assertThat(auth.getAuthorities()).extracting("authority").containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        var principal = (User) auth.getPrincipal();
        assertThat(principal.getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void shouldHaveEmptyAuthoritiesWhenUserHasNoRoles() throws Exception {
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("guest@test.com");
        user.setName("Guest User");
        user.setProvider(AuthProvider.LOCAL);

        var token = tokenService.generateAccessToken(user);

        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
