package com.example.demo.config;

import com.example.demo.entity.Kullanici;
import com.example.demo.repository.KullaniciRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Seeder - Automatically populates database with sample users on startup
 * Runs only if database is empty
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final KullaniciRepository kullaniciRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Check if database is already populated
            if (kullaniciRepository.count() > 0) {
                log.info("Database already contains {} users, skipping seed data", 
                    kullaniciRepository.count());
                return;
            }

            log.info("Database is empty, seeding sample data...");
            
            List<Kullanici> sampleUsers = new ArrayList<>();
            
            // Sample users with different departments
            sampleUsers.add(createUser("Ahmet", "Yılmaz", "ahmet.yilmaz@efsora.com", "IT"));
            sampleUsers.add(createUser("Mehmet", "Kaya", "mehmet.kaya@efsora.com", "Engineering"));
            sampleUsers.add(createUser("Ayşe", "Demir", "ayse.demir@efsora.com", "HR"));
            sampleUsers.add(createUser("Fatma", "Çelik", "fatma.celik@efsora.com", "Finance"));
            sampleUsers.add(createUser("Ali", "Şahin", "ali.sahin@efsora.com", "IT"));
            sampleUsers.add(createUser("Zeynep", "Öztürk", "zeynep.ozturk@efsora.com", "Marketing"));
            sampleUsers.add(createUser("Can", "Aydın", "can.aydin@efsora.com", "Engineering"));
            sampleUsers.add(createUser("Elif", "Yıldız", "elif.yildiz@efsora.com", "Sales"));
            sampleUsers.add(createUser("Burak", "Arslan", "burak.arslan@efsora.com", "IT"));
            sampleUsers.add(createUser("Selin", "Kurt", "selin.kurt@efsora.com", "HR"));
            sampleUsers.add(createUser("Emre", "Özdemir", "emre.ozdemir@efsora.com", "Engineering"));
            sampleUsers.add(createUser("Deniz", "Koç", "deniz.koc@efsora.com", "Finance"));
            sampleUsers.add(createUser("Cem", "Aksoy", "cem.aksoy@efsora.com", "Marketing"));
            sampleUsers.add(createUser("Ece", "Acar", "ece.acar@efsora.com", "Sales"));
            sampleUsers.add(createUser("Mert", "Polat", "mert.polat@efsora.com", "IT"));

            // Add one inactive user (soft deleted)
            Kullanici inactiveUser = createUser("Test", "Inactive", "test.inactive@efsora.com", "Testing");
            inactiveUser.setActive(false);
            sampleUsers.add(inactiveUser);

            kullaniciRepository.saveAll(sampleUsers);
            
            log.info("✅ Successfully seeded {} sample users", sampleUsers.size());
            log.info("   - IT: 4 users");
            log.info("   - Engineering: 3 users");
            log.info("   - HR: 2 users");
            log.info("   - Finance: 2 users");
            log.info("   - Marketing: 2 users");
            log.info("   - Sales: 2 users");
            log.info("   - Inactive: 1 user");
            log.info("🔐 Default password for all users: 'password123'");
            log.info("🎯 Ready for testing! Visit http://localhost:8080/swagger-ui/index.html");
        };
    }

    private Kullanici createUser(String firstName, String lastName, String email, String department) {
        Kullanici kullanici = new Kullanici();
        kullanici.setFirstName(firstName);
        kullanici.setLastName(lastName);
        kullanici.setEmail(email);
        kullanici.setDepartment(department);
        kullanici.setPassword(passwordEncoder.encode("password123")); // Default password for all seed users
        kullanici.setRegistrationDate(LocalDateTime.now());
        kullanici.setActive(true);
        return kullanici;
    }
}
