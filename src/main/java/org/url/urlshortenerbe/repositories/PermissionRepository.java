package org.url.urlshortenerbe.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.url.urlshortenerbe.entities.Permission;

public interface PermissionRepository extends JpaRepository<Permission, String> {}
