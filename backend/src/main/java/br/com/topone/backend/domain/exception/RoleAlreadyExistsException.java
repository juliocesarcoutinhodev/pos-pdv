package br.com.topone.backend.domain.exception;

public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException() {
        super("Role with this name already exists");
    }
}
