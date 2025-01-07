package org.url.urlshortenerbe.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.url.urlshortenerbe.dtos.requests.UserCreationRequest;
import org.url.urlshortenerbe.dtos.requests.UserUpdateRequest;
import org.url.urlshortenerbe.dtos.responses.PageResponse;
import org.url.urlshortenerbe.dtos.responses.PermissionResponse;
import org.url.urlshortenerbe.dtos.responses.RoleResponse;
import org.url.urlshortenerbe.dtos.responses.UserResponse;
import org.url.urlshortenerbe.entities.Role;
import org.url.urlshortenerbe.entities.User;
import org.url.urlshortenerbe.exceptions.AppException;
import org.url.urlshortenerbe.exceptions.ErrorCode;
import org.url.urlshortenerbe.mappers.PermissionMapper;
import org.url.urlshortenerbe.mappers.RoleMapper;
import org.url.urlshortenerbe.mappers.UserMapper;
import org.url.urlshortenerbe.repositories.RoleRepository;
import org.url.urlshortenerbe.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserCreationRequest userCreationRequest) {
        // validate the username first
        if (userRepository.existsByEmail(userCreationRequest.getEmail())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(userCreationRequest);

        // Encode the user password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign role to user
        Role userRole = roleRepository.findById("USER").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOTFOUND));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        user.setRoles(roles);
        user.setBanned(false);

        // Save new user to database
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Guest can't know the system information
        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setRoles(null);
        userResponse.setBanned(null);

        return userResponse;
    }

    public PageResponse<UserResponse> getAll(int page, int size, boolean compact, String type) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<User> users =
                switch (type) {
                    case ("not_banned") -> userRepository.findAllByBannedIs(false, pageable);
                    case ("banned") -> userRepository.findAllByBannedIs(true, pageable);
                    default -> userRepository.findAll(pageable);
                };

        List<UserResponse> userResponseList = users.getContent().stream()
                .map(user -> mapRolesAndPermissionsToUserResponse(user, compact))
                .toList();

        return PageResponse.<UserResponse>builder()
                .items(userResponseList)
                .page(page)
                .records(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    public UserResponse getOne(String userId, boolean compact) {
        User user = getUser(userId);

        return mapRolesAndPermissionsToUserResponse(user, compact);
    }

    public UserResponse update(String userId, UserUpdateRequest userUpdateRequest) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        String email = securityContext.getAuthentication().getName();

        User user = getUser(userId);

        // Only allow admin or user with its correct email to change the information
        if (!email.equals("admin@admin.com") && !user.getEmail().equals(email)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        userMapper.updateUser(user, userUpdateRequest);
        if (!userUpdateRequest.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        Set<Role> roleSet = new HashSet<>();
        userUpdateRequest.getRoles().forEach(role -> {
            Role roleEntity = roleRepository
                    .findById(role.toUpperCase())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOTFOUND));

            roleSet.add(roleEntity);
        });
        user.setRoles(roleSet);

        // save the new roles of user
        user = userRepository.save(user);

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setRoles(roleSet.stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet()));

        return userResponse;
    }

    public void delete(String userId) {
        // find the user first
        User user = getUser(userId);

        // Soft delete user
        user.setBanned(true);

        userRepository.save(user);
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
    }

    private UserResponse mapRolesAndPermissionsToUserResponse(User user, boolean compact) {
        UserResponse userResponse = userMapper.toUserResponse(user);

        // map roles of each user
        userResponse.setRoles(user.getRoles().stream()
                .map(role -> {
                    RoleResponse roleResponse = roleMapper.toRoleResponse(role);

                    if (compact) {
                        return roleResponse;
                    }

                    Set<PermissionResponse> permissionResponseSet = role.getPermissions().stream()
                            .map(permissionMapper::toPermissionResponse)
                            .collect(Collectors.toSet());
                    roleResponse.setPermissions(permissionResponseSet);

                    return roleResponse;
                })
                .collect(Collectors.toSet()));

        return userResponse;
    }
}
