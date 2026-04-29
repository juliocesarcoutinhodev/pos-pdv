package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByIdIncludingDeleted(UUID id);

    Optional<User> findByEmailIncludingDeleted(String email);

    PageResult<User> findAll(UserFilter filter, int page, int size, PageSort sort);

    /**
     * Returns another non-soft-deleted user with the same email,
     * or empty if the email is unique (accounting for the current user).
     */
    Optional<User> findActiveByEmailExcludingId(String email, UUID excludeId);

    /**
     * Returns any other user (including soft-deleted) with the same email,
     * excluding the given ID. Used to satisfy DB unique constraint.
     */
    Optional<User> findByEmailExcludingId(String email, UUID excludeId);

    long countActiveUsers();

}
