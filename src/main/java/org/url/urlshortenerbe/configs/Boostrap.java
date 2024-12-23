package org.url.urlshortenerbe.configs;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.url.urlshortenerbe.entities.Permission;
import org.url.urlshortenerbe.entities.Role;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.repositories.PermissionRepository;
import org.url.urlshortenerbe.repositories.RoleRepository;
import org.url.urlshortenerbe.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class Boostrap {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init() {
        return args -> {
            Set<Permission> adminPermissions = new HashSet<>();
            Set<Permission> userPermissions = new HashSet<>();

            Role adminRole = roleRepository.findById("ADMIN").orElseGet(() -> Role.builder()
                    .name("ADMIN")
                    .description("Administrator role")
                    .build());

            Role userRole = roleRepository
                    .findById("USER")
                    .orElseGet(() ->
                            Role.builder().name("USER").description("User role").build());

            adminPermissions.add(
                    permissionRepository.findById("MANAGE_PERMISSIONS").orElseGet(() -> Permission.builder()
                            .name("MANAGE_PERMISSIONS")
                            .description("Manage permissions for admin")
                            .build()));

            adminPermissions.add(permissionRepository.findById("MANAGE_ROLES").orElseGet(() -> Permission.builder()
                    .name("MANAGE_ROLES")
                    .description("Manage roles for admin")
                    .build()));

            adminPermissions.add(permissionRepository.findById("MANAGE_USERS").orElseGet(() -> Permission.builder()
                    .name("MANAGE_USERS")
                    .description("Manage users for admin")
                    .build()));

            userPermissions.add(permissionRepository.findById("VIEW_URLS").orElseGet(() -> Permission.builder()
                    .name("VIEW_URLS")
                    .description("View products for users")
                    .build()));

            // Save admin permissions to the repository
            permissionRepository.saveAll(adminPermissions);
            adminRole.setPermissions(adminPermissions);

            // Save user permissions
            permissionRepository.saveAll(userPermissions);
            userRole.setPermissions(userPermissions);

            // Save admin role
            roleRepository.save(adminRole);
            // Save user role
            roleRepository.save(userRole);

            if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
                User user = User.builder()
                        .email("admin@admin.com")
                        .password(passwordEncoder.encode("admin"))
                        .roles(Set.of(adminRole))
                        .firstName("admin")
                        .lastName("admin")
                        .build();

                userRepository.save(user);

                log.info("Created admin user with username and password admin:admin");
            }
        };
    }
}
