package br.com.topone.backend.domain.exception;

public class RoleInUseException extends RuntimeException {

    public RoleInUseException() {
        super("Role is assigned to users");
    }
}
