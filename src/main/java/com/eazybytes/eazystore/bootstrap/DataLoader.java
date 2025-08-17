package com.eazybytes.eazystore.bootstrap;

import com.eazybytes.eazystore.entity.Role;
import com.eazybytes.eazystore.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        loadRolesIfNotExist();
    }

    private void loadRolesIfNotExist() {
        List<String> roleNames = Arrays.asList(
            "ROLE_USER",
            "ROLE_ADMIN",
            "ROLE_OPS_ENG",
            "ROLE_QA_ENG"
        );

        for (String roleName : roleNames) {
            if (!roleRepository.findByName(roleName).isPresent()) {
                Role role = new Role();
                role.setName(roleName);
                // Set audit fields manually since we're not using JPA events in CommandLineRunner
                role.setCreatedAt(Instant.now());
                role.setCreatedBy("DBA");
                roleRepository.save(role);
            }
        }
    }
}
