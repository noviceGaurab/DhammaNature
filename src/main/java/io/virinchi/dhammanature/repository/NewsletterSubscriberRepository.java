package io.virinchi.dhammanature.repository;

import io.virinchi.dhammanature.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Integer> {
    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}