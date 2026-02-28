package com.vermeg.platform.supervision_platform.Service;

import com.vermeg.platform.supervision_platform.DTO.UserRequestDTO;
import com.vermeg.platform.supervision_platform.DTO.UserResponseDTO;
import com.vermeg.platform.supervision_platform.Entity.Role;
import com.vermeg.platform.supervision_platform.Entity.RoleName;
import com.vermeg.platform.supervision_platform.Entity.User;
import com.vermeg.platform.supervision_platform.Repository.RoleRepository;
import com.vermeg.platform.supervision_platform.Repository.UserRepository;
import com.vermeg.platform.supervision_platform.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /* =========================
       REGISTER
       ========================= */
    @Override
    public UserResponseDTO register(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Assign VIEWER role by default
        Role defaultRole = roleRepository.findByName(RoleName.ROLE_VIEWER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles(Set.of(defaultRole))
                .build();

        return toDTO(userRepository.save(user));
    }

    /* =========================
       GET BY ID
       ========================= */
    @Override
    public UserResponseDTO getById(Long id) {
        return toDTO(findUser(id));
    }

    /* =========================
       GET BY USERNAME
       ========================= */
    @Override
    public UserResponseDTO getByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return toDTO(user);
    }

    /* =========================
       GET ALL
       ========================= */
    @Override
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /* =========================
       UPDATE
       ========================= */
    @Override
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = findUser(id);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return toDTO(userRepository.save(user));
    }

    /* =========================
       DELETE
       ========================= */
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    /* =========================
       ASSIGN ROLE
       ========================= */
    @Override
    public void assignRole(Long userId, String roleName) {
        User user = findUser(userId);
        Role role = roleRepository.findByName(RoleName.valueOf(roleName))
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        user.getRoles().add(role);
        userRepository.save(user);
    }

    /* =========================
       HELPERS
       ========================= */
    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponseDTO toDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
