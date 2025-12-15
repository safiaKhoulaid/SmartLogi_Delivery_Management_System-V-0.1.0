package com.smartlogi.sdms.infrastructure.utils.seeder;

import com.smartlogi.sdms.domain.model.entity.Permission;
import com.smartlogi.sdms.domain.model.enums.Role;
import com.smartlogi.sdms.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionSeeder implements CommandLineRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // كنشرجيو الـ Permissions غير إلا كانت لاباز خاوية
        if (permissionRepository.count() == 0) {
            seedPermissionsAndAssignToRoles();
        }
    }

    private void seedPermissionsAndAssignToRoles() {
        log.info("🚀 Bdaya dyal Seeding Permissions & Roles...");

        // 1. لائحة الصلاحيات كاملة
        List<String> allPermissions = Arrays.asList(
                // --- Colis ---
                "COLIS_READ", "COLIS_CREATE", "COLIS_UPDATE", "COLIS_DELETE", "COLIS_UPDATE_STATUS",
                // --- Tournées & Missions ---
                "TOURNEE_READ", "TOURNEE_GENERATE",
                "MISSION_READ", "MISSION_ASSIGN", "MISSION_UPDATE_STATUS",
                // --- Users & Livreurs ---
                "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE",
                "LIVREUR_MANAGE",
                // --- Zones ---
                "ZONE_READ", "ZONE_MANAGE"
        );

        // 2. البوكل باش نكرييو ونفرقو الصلاحيات
        for (String permName : allPermissions) {

            // A. إنشاء الـ Permission فـ لاباز
            Permission permission;
            if (!permissionRepository.existsByName(permName)) {
                permission = permissionRepository.save(Permission.builder().name(permName).build());
            } else {
                // إلا كانت ديجا كاينة، خاصنا نجيبوها (ملاحظة: خاص findByName تكون فالريبو، ولكن هنا غنكملو بالـ save)
                continue;
            }

            // B. توزيع الصلاحيات على لي رول (Assignation)

            // 1. ADMIN: كياخد كلشي (God Mode)
            assignToRole(Role.ADMIN, permission);

            // 2. GESTIONNAIRE: كياخد Colis, Tournée, Mission, Users (بلا ما يمسح)
            if (isGestionnairePerm(permName)) {
                assignToRole(Role.GESTIONNAIRE, permission);
            }

            // 3. LIVREUR: كيشوف Missions ديالو وكيبدل الستاتي
            if (Set.of("MISSION_READ", "MISSION_UPDATE_STATUS", "TOURNEE_READ").contains(permName)) {
                assignToRole(Role.LIVREUR, permission);
            }

            // 4. USER (Client): كيكريي وكشوف Colis ديالو
            if (Set.of("COLIS_READ", "COLIS_CREATE").contains(permName)) {
                assignToRole(Role.USER, permission);
            }
        }

        log.info("✅ Permissions seeded and assigned successfully!");
    }

    // Fonction Helper باش نزيدو الـ Perm للرول فـ الطابل role_permissions
    private void assignToRole(Role role, Permission permission) {
        try {
            permissionRepository.addPermissionToRole(role.name(), permission.getId());
        } catch (Exception e) {
            log.warn("Permission {} deja assignée au role {}", permission.getName(), role.name());
        }
    }

    // Logic باش نحددو شنو نعطيو للـ Gestionnaire
    private boolean isGestionnairePerm(String p) {
        // الجيستيونير كيدير كلشي فـ Colis/Mission/Tournée/Zone من غير المسح (Delete)
        boolean isDomainPerm = p.startsWith("COLIS_") || p.startsWith("TOURNEE_")
                || p.startsWith("MISSION_") || p.startsWith("ZONE_")
                || p.equals("LIVREUR_MANAGE") || p.startsWith("USER_");

        boolean isDelete = p.contains("_DELETE");

        return isDomainPerm && !isDelete; // كياخذ الصلاحيات ولكن ميمكنش يمسح
    }
}