package org.url.urlshortenerbe.configs;

import java.util.HashSet;
import java.util.List;
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
            Set<Role> adminRoles = new HashSet<>();

            Set<Permission> adminPermissions = new HashSet<>();
            Set<Permission> userPermissions = new HashSet<>();
            Set<Permission> managerPermissions = new HashSet<>();

            List<String> roles = List.of("ADMIN", "MANAGER", "USER");
            List<String> permissions = List.of(
                    // USER
                    "CREATE_USER",
                    "READ_USER",
                    "UPDATE_USER",
                    "DELETE_USER",
                    "MANAGE_USER",

                    // ROLE
                    "CREATE_ROLE",
                    "READ_ROLE",
                    "UPDATE_ROLE",
                    "DELETE_ROLE",
                    "MANAGE_ROLE",

                    // PERMISSIONS
                    "CREATE_PERMISSION",
                    "READ_PERMISSION",
                    "UPDATE_PERMISSION",
                    "DELETE_PERMISSION",
                    "MANAGE_PERMISSION",

                    // URL
                    "CREATE_URL",
                    "READ_URL",
                    "UPDATE_URL",
                    "DELETE_URL",
                    "MANAGE_URL",

                    // CAMPAIGN
                    "CREATE_CAMPAIGN",
                    "READ_CAMPAIGN",
                    "UPDATE_CAMPAIGN",
                    "DELETE_CAMPAIGN",
                    "MANAGE_CAMPAIGN");

            permissions.forEach(permission -> {
                Permission permissionEntity = permissionRepository
                        .findById(permission)
                        .orElse(Permission.builder()
                                .name(permission)
                                .description(permission)
                                .build());

                permissionEntity = permissionRepository.save(permissionEntity);

                if (permission.contains("MANAGE")) {
                    adminPermissions.add(permissionEntity);
                    // skip this if face the permission with "MANAGE" in it
                    return;
                }

                if (permission.contains("URL") || permission.equals("UPDATE_USER")) {
                    userPermissions.add(permissionEntity);
                    managerPermissions.add(permissionEntity);
                }

                if (permission.contains("CAMPAIGN")) {
                    managerPermissions.add(permissionEntity);
                }
            });

            roles.forEach(role -> {
                Role roleEntity = roleRepository
                        .findById(role)
                        .orElse(Role.builder().name(role).description(role).build());

                switch (role) {
                    case "ADMIN" -> roleEntity.setPermissions(adminPermissions);
                    case "MANAGER" -> roleEntity.setPermissions(managerPermissions);
                    case "USER" -> roleEntity.setPermissions(userPermissions);
                }

                roleEntity = roleRepository.save(roleEntity);

                if (role.equals("ADMIN")) {
                    adminRoles.add(roleEntity);
                }
            });

            if (userRepository.existsByEmail("admin@admin.com")) {
                return;
            }

            User user = User.builder()
                    .email("admin@admin.com")
                    .password(passwordEncoder.encode("admin"))
                    .roles(adminRoles)
                    .firstName("admin")
                    .lastName("admin")
                    .build();

            userRepository.save(user);

            log.info("Created admin user with username and password admin:admin");
        };
    }
}
