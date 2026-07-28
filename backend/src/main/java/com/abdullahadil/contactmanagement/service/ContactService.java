package com.abdullahadil.contactmanagement.service;

import com.abdullahadil.contactmanagement.dto.ContactEmailResponse;
import com.abdullahadil.contactmanagement.dto.ContactPhoneResponse;
import com.abdullahadil.contactmanagement.dto.ContactRequest;
import com.abdullahadil.contactmanagement.dto.ContactResponse;
import com.abdullahadil.contactmanagement.entity.Contact;
import com.abdullahadil.contactmanagement.entity.ContactEmail;
import com.abdullahadil.contactmanagement.entity.ContactPhone;
import com.abdullahadil.contactmanagement.exception.ResourceNotFoundException;
import com.abdullahadil.contactmanagement.repository.ContactRepository;
import com.abdullahadil.contactmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public ContactResponse create(Long ownerId, ContactRequest request) {
        Contact contact = new Contact();
        contact.setOwner(userRepository.getReferenceById(ownerId));
        applyRequest(contact, request);

        Contact saved = contactRepository.save(contact);
        log.info("Owner {} created contact {}", ownerId, saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ContactResponse> list(Long ownerId, String search, Pageable pageable) {
        Page<Contact> page = (search == null || search.isBlank())
                ? contactRepository.findByOwnerId(ownerId, pageable)
                : contactRepository.search(ownerId, search, pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ContactResponse getById(Long ownerId, Long contactId) {
        return toResponse(findOwnedContact(ownerId, contactId));
    }

    public ContactResponse update(Long ownerId, Long contactId, ContactRequest request) {
        Contact contact = findOwnedContact(ownerId, contactId);
        applyRequest(contact, request);
        // Flush so newly-added emails/phones get their generated ids before
        // we map the response - otherwise they'd serialize with a null id
        // until the transaction commits.
        contactRepository.saveAndFlush(contact);
        log.info("Owner {} updated contact {}", ownerId, contactId);
        return toResponse(contact);
    }

    public void delete(Long ownerId, Long contactId) {
        Contact contact = findOwnedContact(ownerId, contactId);
        contactRepository.delete(contact);
        log.info("Owner {} deleted contact {}", ownerId, contactId);
    }

    private void applyRequest(Contact contact, ContactRequest request) {
        contact.setFirstName(request.firstName());
        contact.setLastName(request.lastName());
        contact.setTitle(request.title());

        contact.getEmails().clear();
        contact.getPhones().clear();

        for (var emailRequest : nullSafe(request.emails())) {
            contact.addEmail(new ContactEmail(emailRequest.label(), emailRequest.email()));
        }
        for (var phoneRequest : nullSafe(request.phones())) {
            contact.addPhone(new ContactPhone(phoneRequest.label(), phoneRequest.phoneNumber()));
        }
    }

    private Contact findOwnedContact(Long ownerId, Long contactId) {
        Contact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));

        // Deliberately the same "not found" message as above rather than a 403 -
        // don't reveal that a contact exists if it belongs to someone else.
        if (!contact.getOwner().getId().equals(ownerId)) {
            throw new ResourceNotFoundException("Contact not found");
        }
        return contact;
    }

    private <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }

    private ContactResponse toResponse(Contact contact) {
        List<ContactEmailResponse> emails = contact.getEmails().stream()
                .map(e -> new ContactEmailResponse(e.getId(), e.getLabel(), e.getEmail()))
                .toList();
        List<ContactPhoneResponse> phones = contact.getPhones().stream()
                .map(p -> new ContactPhoneResponse(p.getId(), p.getLabel(), p.getPhoneNumber()))
                .toList();

        return new ContactResponse(
                contact.getId(),
                contact.getFirstName(),
                contact.getLastName(),
                contact.getTitle(),
                emails,
                phones,
                contact.getCreatedAt(),
                contact.getUpdatedAt());
    }
}
