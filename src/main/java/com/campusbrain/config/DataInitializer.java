package com.campusbrain.config;

import com.campusbrain.entity.AdminUser;
import com.campusbrain.repository.AdminUserRepository;
import com.campusbrain.security.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminUserRepository adminUserRepository;
    private final PasswordService passwordService;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public DataInitializer(AdminUserRepository adminUserRepository,
                           PasswordService passwordService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordService = passwordService;
    }

    @Override
    public void run(String... args) {
        initAdminUser();
        ensureUploadDirectory();
    }

    private void initAdminUser() {
        if (!adminUserRepository.existsByUsername("admin")) {
            String salt = passwordService.generateSalt();
            String passwordHash = passwordService.hashPassword("admin123", salt);

            AdminUser admin = new AdminUser(
                    "admin",
                    passwordHash,
                    salt,
                    "Chief Academic Administrator",
                    "admin@campusbrain.edu"
            );
            adminUserRepository.save(admin);
            log.info("Default Admin account initialized successfully. Username: admin");
        }
    }

    private void ensureUploadDirectory() {
        try {
            Path rootUploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(rootUploadPath);
        } catch (Exception e) {
            log.error("Could not create uploads directory: {}", e.getMessage());
        }
    }
}
