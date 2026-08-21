package com.abdullahadil.contactmanagement.controller;

import com.abdullahadil.contactmanagement.config.SecurityConfig;
import com.abdullahadil.contactmanagement.dto.ContactRequest;
import com.abdullahadil.contactmanagement.dto.ContactResponse;
import com.abdullahadil.contactmanagement.entity.User;
import com.abdullahadil.contactmanagement.exception.ResourceNotFoundException;
import com.abdullahadil.contactmanagement.security.JwtService;
import com.abdullahadil.contactmanagement.security.UserDetailsServiceImpl;
import com.abdullahadil.contactmanagement.security.UserPrincipal;
import com.abdullahadil.contactmanagement.service.ContactService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
@Import(SecurityConfig.class)
class ContactControllerTest {

    private static final Long OWNER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    // Required by the security filter chain, not by these tests directly.
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    /** Stands in for the principal the JWT filter would normally set up. */
    private Authentication signedInUser() {
        User user = User.builder().id(OWNER_ID).email("owner@example.com").passwordHash("hash").build();
        UserPrincipal principal = new UserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private ContactResponse sampleContact() {
        return new ContactResponse(10L, "Ayesha", "Khan", "Designer",
                List.of(), List.of(), Instant.now(), Instant.now());
    }

    @Test
    void listReturnsPageOfContactsForTheSignedInUser() throws Exception {
        Page<ContactResponse> page = new PageImpl<>(List.of(sampleContact()), PageRequest.of(0, 10), 1);
        when(contactService.list(eq(OWNER_ID), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/contacts").with(authentication(signedInUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Ayesha"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listPassesSearchTermThrough() throws Exception {
        when(contactService.list(eq(OWNER_ID), eq("khan"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        mockMvc.perform(get("/api/contacts").param("search", "khan").with(authentication(signedInUser())))
                .andExpect(status().isOk());

        verify(contactService).list(eq(OWNER_ID), eq("khan"), any());
    }

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/contacts"))
                .andExpect(status().isForbidden());

        verify(contactService, never()).list(any(), any(), any());
    }

    @Test
    void createReturnsCreatedContact() throws Exception {
        when(contactService.create(eq(OWNER_ID), any(ContactRequest.class))).thenReturn(sampleContact());

        mockMvc.perform(post("/api/contacts")
                        .with(authentication(signedInUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ayesha","lastName":"Khan","title":"Designer",
                                 "emails":[{"label":"WORK","email":"a@work.com"}],"phones":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createRejectsBlankFirstName() throws Exception {
        mockMvc.perform(post("/api/contacts")
                        .with(authentication(signedInUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Khan","emails":[],"phones":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verify(contactService, never()).create(any(), any());
    }

    @Test
    void getByIdReturnsContact() throws Exception {
        when(contactService.getById(OWNER_ID, 10L)).thenReturn(sampleContact());

        mockMvc.perform(get("/api/contacts/10").with(authentication(signedInUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Khan"));
    }

    @Test
    void getByIdReturnsNotFoundForSomeoneElsesContact() throws Exception {
        when(contactService.getById(OWNER_ID, 99L))
                .thenThrow(new ResourceNotFoundException("Contact not found"));

        mockMvc.perform(get("/api/contacts/99").with(authentication(signedInUser())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Contact not found"));
    }

    @Test
    void updateReturnsUpdatedContact() throws Exception {
        when(contactService.update(eq(OWNER_ID), eq(10L), any(ContactRequest.class)))
                .thenReturn(sampleContact());

        mockMvc.perform(put("/api/contacts/10")
                        .with(authentication(signedInUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ayesha","lastName":"Khan","emails":[],"phones":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/contacts/10").with(authentication(signedInUser())))
                .andExpect(status().isNoContent());

        verify(contactService).delete(OWNER_ID, 10L);
    }

    @Test
    void deleteReturnsNotFoundForSomeoneElsesContact() throws Exception {
        doThrow(new ResourceNotFoundException("Contact not found"))
                .when(contactService).delete(OWNER_ID, 99L);

        mockMvc.perform(delete("/api/contacts/99").with(authentication(signedInUser())))
                .andExpect(status().isNotFound());
    }
}
