package com.abdullahadil.contactmanagement.config;

import com.abdullahadil.contactmanagement.entity.Contact;
import com.abdullahadil.contactmanagement.entity.ContactEmail;
import com.abdullahadil.contactmanagement.entity.ContactLabel;
import com.abdullahadil.contactmanagement.entity.ContactPhone;
import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.repository.ContactRepository;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Puts a demo account and a few contacts in the in-memory database so the app
 * has something to show on startup. Dev only - this never runs against the
 * prod profile, so no demo credentials can end up in a real database.
 */
@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private static final String DEMO_EMAIL = "demo@example.com";
    private static final String DEMO_PASSWORD = "password123";

    // "first last title", with the title's spaces written as hyphens.
    // Enough contacts to push the list onto a second page.
    private static final List<String> DEMO_PEOPLE = List.of(
            "Ayesha Khan Product-Designer",
            "Bilal Ahmed Software-Engineer",
            "Sana Malik Project-Manager",
            "Hamza Iqbal QA-Engineer",
            "Zara Sheikh Data-Analyst",
            "Usman Raza DevOps-Engineer",
            "Fatima Noor UX-Researcher",
            "Omar Siddiqui Business-Analyst",
            "Hina Aslam Scrum-Master",
            "Danish Qureshi Backend-Engineer",
            "Maryam Javed Frontend-Engineer",
            "Tariq Mahmood Solutions-Architect");

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository, ContactRepository contactRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }

        User demo = userRepository.save(User.builder()
                .email(DEMO_EMAIL)
                .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                .build());

        for (int i = 0; i < DEMO_PEOPLE.size(); i++) {
            String[] person = DEMO_PEOPLE.get(i).split(" ");
            String first = person[0];
            String last = person[1];
            String title = person[2].replace('-', ' ');

            Contact contact = new Contact();
            contact.setOwner(demo);
            contact.setFirstName(first);
            contact.setLastName(last);
            contact.setTitle(title);

            String handle = (first + "." + last).toLowerCase();
            contact.addEmail(new ContactEmail(ContactLabel.WORK, handle + "@company.com"));
            contact.addEmail(new ContactEmail(ContactLabel.PERSONAL, handle + "@example.com"));
            contact.addPhone(new ContactPhone(ContactLabel.WORK, "021-111-" + (1000 + i)));
            contact.addPhone(new ContactPhone(ContactLabel.HOME, "0300-555-" + (1000 + i)));

            contactRepository.save(contact);
        }

        log.info("Seeded demo account {} with {} contacts", DEMO_EMAIL, DEMO_PEOPLE.size());
    }
}
