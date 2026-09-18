package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Integer> {

    @Query("SELECT c FROM ContactMessage c ORDER BY c.createdAt DESC")
    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.resolved = false")
    long countUnresolved();
}