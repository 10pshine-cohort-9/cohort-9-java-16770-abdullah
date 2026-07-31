package com.abdullahadil.contactmanagement.controller;

import com.abdullahadil.contactmanagement.dto.ContactRequest;
import com.abdullahadil.contactmanagement.dto.ContactResponse;
import com.abdullahadil.contactmanagement.security.UserPrincipal;
import com.abdullahadil.contactmanagement.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ContactRequest request) {
        ContactResponse created = contactService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<ContactResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return ResponseEntity.ok(contactService.list(principal.getId(), search, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(contactService.getById(principal.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(contactService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id) {
        contactService.delete(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
