package br.com.topone.backend.domain.exception;

public class SystemRoleDeletionNotAllowedException extends RuntimeException {

    public SystemRoleDeletionNotAllowedException() {
        super("System role cannot be deleted");
    }
}
