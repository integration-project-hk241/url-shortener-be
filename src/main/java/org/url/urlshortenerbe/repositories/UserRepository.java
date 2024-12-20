package org.url.urlshortenerbe.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.User;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
