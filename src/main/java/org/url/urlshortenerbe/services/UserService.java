package org.url.urlshortenerbe.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        if (userRepository.existsByUsername(userCreationRequest.getUsername())) {
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

        // Save new user to database
        user = userRepository.save(user);

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setRoles(roles.stream().map(roleMapper::toRoleResponse).collect(Collectors.toSet()));

        return userResponse;
    }

    public PageResponse<UserResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> users = userRepository.findAll(pageable);

        List<UserResponse> userResponseList = users.getContent().stream()
                .map(user -> {
                    UserResponse userResponse = userMapper.toUserResponse(user);

                    // map roles of each user
                    userResponse.setRoles(user.getRoles().stream()
                            .map(role -> {
                                RoleResponse roleResponse = roleMapper.toRoleResponse(role);

                                Set<PermissionResponse> permissionResponseSet = role.getPermissions().stream()
                                        .map(permissionMapper::toPermissionResponse)
                                        .collect(Collectors.toSet());
                                roleResponse.setPermissions(permissionResponseSet);

                                return roleResponse;
                            })
                            .collect(Collectors.toSet()));

                    return userResponse;
                })
                .toList();

        return PageResponse.<UserResponse>builder()
                .items(userResponseList)
                .page(page)
                .records(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .build();
    }

    public UserResponse getOne(String userId) {
        User user = getUser(userId);

        UserResponse userResponse = userMapper.toUserResponse(user);

        // map roles of each user
        userResponse.setRoles(user.getRoles().stream()
                .map(role -> {
                    RoleResponse roleResponse = roleMapper.toRoleResponse(role);

                    Set<PermissionResponse> permissionResponseSet = role.getPermissions().stream()
                            .map(permissionMapper::toPermissionResponse)
                            .collect(Collectors.toSet());
                    roleResponse.setPermissions(permissionResponseSet);

                    return roleResponse;
                })
                .collect(Collectors.toSet()));

        return userResponse;
    }

    public UserResponse update(String userId, UserUpdateRequest userUpdateRequest) {
        User user = getUser(userId);

        userMapper.updateUser(user, userUpdateRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

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

        userRepository.delete(user);
    }

    private User getUser(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
    }
}
