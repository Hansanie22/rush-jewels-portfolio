package lk.dio.rush_jewels.config;

import lk.dio.rush_jewels.model.Status;
import lk.dio.rush_jewels.model.User;
import lk.dio.rush_jewels.repository.StatusRepository;
import lk.dio.rush_jewels.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StatusRepository statusRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StatusRepository statusRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            ensureDemoUser("customer@gmail.com", "Customer@1234", "Amaya", "Senanayake", "0771234567");
            ensureDemoUser("dilshan@gmail.com", "Customer@1234", "Dilshan", "Wickramasinghe", "0719876543");
            ensureDemoUser("demo@rushjewels.com", "Customer@1234", "Demo", "User", "0779998888");
        } catch (Exception e) {
            log.warn("DataInitializer warning: {}", e.getMessage());
        }
    }

    private void ensureDemoUser(String email, String rawPassword, String fname, String lname, String mobile) {
        Optional<User> existing = userRepository.findByEmail(email);
        Status activeStatus = statusRepository.findById(1).orElse(null);

        if (existing.isPresent()) {
            User user = existing.get();
            boolean needsUpdate = false;

            if (user.getPassword() == null || !user.getPassword().startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(rawPassword));
                needsUpdate = true;
            }
            if (!"Verified".equalsIgnoreCase(user.getVerification())) {
                user.setVerification("Verified");
                needsUpdate = true;
            }
            if (user.getStatus() == null && activeStatus != null) {
                user.setStatus(activeStatus);
                needsUpdate = true;
            }

            if (needsUpdate) {
                userRepository.save(user);
                log.info("Initialized/Updated demo user password & verification for: {}", email);
            }
        } else if (activeStatus != null) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFname(fname);
            user.setLname(lname);
            user.setMobile(mobile);
            user.setStatus(activeStatus);
            user.setVerification("Verified");
            user.setCreatedAt(new Date());
            user.setLoginProvider("LOCAL");
            user.setType("USER");
            userRepository.save(user);
            log.info("Created new demo user: {}", email);
        }
    }
}
