package org.url.urlshortenerbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Role;

public interface RoleRepository extends JpaRepository<Role, String> {}
