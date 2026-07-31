package com.abdullahadil.contactmanagement.repository;

import com.abdullahadil.contactmanagement.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByOwnerId(Long ownerId, Pageable pageable);

    @Query("""
            SELECT c FROM Contact c
            WHERE c.owner.id = :ownerId
              AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Contact> search(@Param("ownerId") Long ownerId, @Param("search") String search, Pageable pageable);
}
