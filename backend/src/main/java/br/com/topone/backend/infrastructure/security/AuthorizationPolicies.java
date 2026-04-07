package br.com.topone.backend.infrastructure.security;

public final class AuthorizationPolicies {

    public static final String AUTHENTICATED = "isAuthenticated()";
    public static final String ADMIN_ONLY = "hasRole('ADMIN')";

    private AuthorizationPolicies() {
    }
}
