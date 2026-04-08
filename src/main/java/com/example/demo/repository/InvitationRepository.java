package com.example.demo.repository;

import com.example.demo.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByToken(String token);
    boolean existsByEmailAndUsedFalse(String email);
}
