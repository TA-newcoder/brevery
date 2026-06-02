package com.brevery.repository;

import com.brevery.entity.EmailVerification;
import com.brevery.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByTokenAndTypeAndIsUsedFalse(String token, TokenType type);
}
