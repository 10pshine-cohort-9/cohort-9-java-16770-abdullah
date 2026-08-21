package com.abdullahadil.contactmanagement.repository;

import com.abdullahadil.contactmanagement.entity.Contact;
import com.abdullahadil.contactmanagement.entity.ContactEmail;
import com.abdullahadil.contactmanagement.entity.ContactLabel;
import com.abdullahadil.contactmanagement.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContactRepository contactRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = persistUser("owner@example.com");
        otherUser = persistUser("other@example.com");

        persistContact(owner, "Ayesha", "Khan");
        persistContact(owner, "Bilal", "Ahmed");
        persistContact(owner, "Sana", "Khanum");
        // Belongs to somebody else - must never show up in the owner's results.
        persistContact(otherUser, "Ayesha", "Khan");
    }

    private User persistUser(String email) {
        User user = User.builder().email(email).passwordHash("hashed").build();
        return entityManager.persist(user);
    }

    private Contact persistContact(User contactOwner, String firstName, String lastName) {
        Contact contact = new Contact();
        contact.setOwner(contactOwner);
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.addEmail(new ContactEmail(ContactLabel.WORK, firstName.toLowerCase() + "@work.com"));
        return entityManager.persist(contact);
    }

    @Test
    void findByOwnerIdReturnsOnlyThatOwnersContacts() {
        Page<Contact> page = contactRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent())
                .allSatisfy(contact -> assertThat(contact.getOwner().getId()).isEqualTo(owner.getId()));
    }

    @Test
    void findByOwnerIdPaginates() {
        Page<Contact> firstPage = contactRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
    }

    @Test
    void searchMatchesFirstNameIgnoringCase() {
        Page<Contact> page = contactRepository.search(owner.getId(), "ayesha", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getFirstName()).isEqualTo("Ayesha");
    }

    @Test
    void searchMatchesPartialLastName() {
        Page<Contact> page = contactRepository.search(owner.getId(), "khan", PageRequest.of(0, 10));

        // Matches both "Khan" and "Khanum".
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchNeverLeaksAnotherUsersContacts() {
        Page<Contact> page = contactRepository.search(otherUser.getId(), "khan", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().getOwner().getId()).isEqualTo(otherUser.getId());
    }

    @Test
    void searchReturnsNothingWhenNoNameMatches() {
        Page<Contact> page = contactRepository.search(owner.getId(), "zzzz", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void savedContactCascadesItsEmails() {
        Contact saved = contactRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 1)).getContent().getFirst();

        assertThat(saved.getEmails()).isNotEmpty();
        assertThat(saved.getEmails().getFirst().getId()).isNotNull();
    }
}
