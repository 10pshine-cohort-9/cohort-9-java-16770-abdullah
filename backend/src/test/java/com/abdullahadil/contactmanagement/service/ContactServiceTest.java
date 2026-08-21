package com.abdullahadil.contactmanagement.service;

import com.abdullahadil.contactmanagement.dto.ContactEmailRequest;
import com.abdullahadil.contactmanagement.dto.ContactPhoneRequest;
import com.abdullahadil.contactmanagement.dto.ContactRequest;
import com.abdullahadil.contactmanagement.dto.ContactResponse;
import com.abdullahadil.contactmanagement.entity.Contact;
import com.abdullahadil.contactmanagement.entity.ContactLabel;
import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.exception.ResourceNotFoundException;
import com.abdullahadil.contactmanagement.repository.ContactRepository;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactService contactService;

    private User owner(Long id) {
        return User.builder().id(id).email("owner" + id + "@example.com").passwordHash("hash").build();
    }

    private Contact contactOwnedBy(Long ownerId, Long contactId) {
        Contact contact = new Contact();
        contact.setId(contactId);
        contact.setOwner(owner(ownerId));
        contact.setFirstName("Ayesha");
        contact.setLastName("Khan");
        contact.setTitle("Designer");
        return contact;
    }

    @Test
    void createSavesContactAgainstTheOwner() {
        ContactRequest request = new ContactRequest(
                "Ayesha", "Khan", "Designer",
                List.of(new ContactEmailRequest(ContactLabel.WORK, "a@work.com")),
                List.of(new ContactPhoneRequest(ContactLabel.HOME, "111")));

        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner(OWNER_ID));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact contact = invocation.getArgument(0);
            contact.setId(10L);
            return contact;
        });

        ContactResponse response = contactService.create(OWNER_ID, request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.firstName()).isEqualTo("Ayesha");
        assertThat(response.emails()).hasSize(1);
        assertThat(response.emails().getFirst().email()).isEqualTo("a@work.com");
        assertThat(response.phones()).hasSize(1);
        assertThat(response.phones().getFirst().phoneNumber()).isEqualTo("111");
    }

    @Test
    void createHandlesMissingEmailAndPhoneLists() {
        ContactRequest request = new ContactRequest("No", "Contacts", null, null, null);

        when(userRepository.getReferenceById(OWNER_ID)).thenReturn(owner(OWNER_ID));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContactResponse response = contactService.create(OWNER_ID, request);

        assertThat(response.emails()).isEmpty();
        assertThat(response.phones()).isEmpty();
    }

    @Test
    void listWithoutSearchUsesOwnerLookup() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contactOwnedBy(OWNER_ID, 10L)));
        when(contactRepository.findByOwnerId(OWNER_ID, pageable)).thenReturn(page);

        Page<ContactResponse> result = contactService.list(OWNER_ID, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(contactRepository, never()).search(any(), any(), any());
    }

    @Test
    void listWithBlankSearchStillUsesOwnerLookup() {
        Pageable pageable = PageRequest.of(0, 10);
        when(contactRepository.findByOwnerId(OWNER_ID, pageable)).thenReturn(Page.empty(pageable));

        contactService.list(OWNER_ID, "   ", pageable);

        verify(contactRepository, never()).search(any(), any(), any());
    }

    @Test
    void listWithSearchUsesSearchQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contactOwnedBy(OWNER_ID, 10L)));
        when(contactRepository.search(OWNER_ID, "khan", pageable)).thenReturn(page);

        Page<ContactResponse> result = contactService.list(OWNER_ID, "khan", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(contactRepository, never()).findByOwnerId(eq(OWNER_ID), any());
    }

    @Test
    void getByIdReturnsOwnedContact() {
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contactOwnedBy(OWNER_ID, 10L)));

        ContactResponse response = contactService.getById(OWNER_ID, 10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.lastName()).isEqualTo("Khan");
    }

    @Test
    void getByIdRejectsContactBelongingToSomeoneElse() {
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contactOwnedBy(OTHER_USER_ID, 10L)));

        assertThatThrownBy(() -> contactService.getById(OWNER_ID, 10L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByIdRejectsMissingContact() {
        when(contactRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getById(OWNER_ID, 404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateReplacesFieldsAndChildRows() {
        Contact existing = contactOwnedBy(OWNER_ID, 10L);
        existing.addEmail(new com.abdullahadil.contactmanagement.entity.ContactEmail(
                ContactLabel.WORK, "old@work.com"));

        ContactRequest request = new ContactRequest(
                "Ayesha", "Khan-Malik", "Lead Designer",
                List.of(new ContactEmailRequest(ContactLabel.PERSONAL, "new@home.com")),
                List.of());

        when(contactRepository.findById(10L)).thenReturn(Optional.of(existing));

        ContactResponse response = contactService.update(OWNER_ID, 10L, request);

        assertThat(response.lastName()).isEqualTo("Khan-Malik");
        assertThat(response.title()).isEqualTo("Lead Designer");
        assertThat(response.emails()).hasSize(1);
        assertThat(response.emails().getFirst().email()).isEqualTo("new@home.com");
        verify(contactRepository).saveAndFlush(existing);
    }

    @Test
    void updateRejectsContactBelongingToSomeoneElse() {
        ContactRequest request = new ContactRequest("X", "Y", null, List.of(), List.of());
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contactOwnedBy(OTHER_USER_ID, 10L)));

        assertThatThrownBy(() -> contactService.update(OWNER_ID, 10L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(contactRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteRemovesOwnedContact() {
        Contact existing = contactOwnedBy(OWNER_ID, 10L);
        when(contactRepository.findById(10L)).thenReturn(Optional.of(existing));

        contactService.delete(OWNER_ID, 10L);

        verify(contactRepository).delete(existing);
    }

    @Test
    void deleteRejectsContactBelongingToSomeoneElse() {
        when(contactRepository.findById(10L)).thenReturn(Optional.of(contactOwnedBy(OTHER_USER_ID, 10L)));

        assertThatThrownBy(() -> contactService.delete(OWNER_ID, 10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(contactRepository, never()).delete(any());
    }
}
