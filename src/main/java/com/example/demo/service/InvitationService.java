package com.example.demo.service;

import com.example.demo.entity.Invitation;
import java.util.Optional;

public interface InvitationService {
    Invitation createInvitation(String email, String role, Long expiresInMinutes);
    Optional<Invitation> verifyToken(String token);
    boolean acceptInvitation(String token);
    boolean existsActiveInvitation(String email);
}
