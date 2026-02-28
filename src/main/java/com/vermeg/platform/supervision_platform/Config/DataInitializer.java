package com.vermeg.platform.supervision_platform.Config;

import com.vermeg.platform.supervision_platform.Entity.Role;
import com.vermeg.platform.supervision_platform.Entity.RoleName;
import com.vermeg.platform.supervision_platform.Entity.User;
import com.vermeg.platform.supervision_platform.Repository.RoleRepository;
import com.vermeg.platform.supervision_platform.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // Create roles if they don't exist
        createRoleIfNotExists(RoleName.ROLE_ADMIN);
        createRoleIfNotExists(RoleName.ROLE_MANAGER);
        createRoleIfNotExists(RoleName.ROLE_VIEWER);

        // Create default admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();
            User admin = User.builder().username("admin").email("admin@vermeg.com")
                    .password(passwordEncoder.encode("Admin@12345")).roles(Set.of(adminRole)).build();
            userRepository.save(admin);
            System.out.println("✅ Default admin user created — username: admin, password: Admin@12345");
        }
    }
    private void createRoleIfNotExists(RoleName roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            roleRepository.save(Role.builder().name(roleName).build());
            System.out.println("✅ Role created: " + roleName);
        }
    }
}
