package org.url.urlshortenerbe.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.User;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String username);

    Optional<User> findByEmail(String username);
}
