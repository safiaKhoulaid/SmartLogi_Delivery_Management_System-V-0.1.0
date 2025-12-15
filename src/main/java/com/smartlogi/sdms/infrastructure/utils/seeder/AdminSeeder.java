package com.smartlogi.sdms.infrastructure.utils.seeder;

import com.smartlogi.sdms.domain.model.entity.users.Admin;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.model.vo.Adresse;
import com.smartlogi.sdms.domain.model.vo.Telephone;
import com.smartlogi.sdms.domain.repository.AdminRepository;
import com.smartlogi.sdms.domain.repository.BaseUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final BaseUserRepository baseUserRepository; // باش نتيستيو واش الإيميل كاين
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
    }

    private void seedAdmin() {
        String adminEmail = "admin@smartlogi.com";

        // 1. كنتأكدوا واش هاد Admin ديجا كاين باش ما نعاودوش نكرييوه
        if (baseUserRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists: {}", adminEmail);
            return;
        }

        log.info("Seeding Admin user...");

        // 2. كنوجدو Value Objects (حيت عندك Embeddable و Records)
        // خاص المعلومات تكون صحيحة باش دوز من Validation (تيليفون مغربي، كود بوستال...)
        Telephone telephone = new Telephone("+212", "600000000");
        Adresse adresse = new Adresse(
                "1",
                "AVENUE MOHAMMED V",
                "RABAT",
                "10000",
                "MAROC",
                34.020882,
                -6.841650
        );

        // 3. كنصاوبو الـ Entity ديال Admin
        Admin admin = new Admin();
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("password123")); // المودباس هو password123
        admin.setRole(Role.ADMIN);
        admin.setTelephone(telephone);
        admin.setAdresse(adresse);

        // 4. كنسجلوه فـ لاباز
        adminRepository.save(admin);

        log.info("✅ Admin user created successfully with email: {} and password: password123", adminEmail);
    }
}