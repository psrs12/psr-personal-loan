package com.personalloan.offeracceptance.domain.offer;

import com.personalloan.offeracceptance.domain.exception.AlreadySignedException;
import com.personalloan.offeracceptance.domain.exception.MandatoryDeclarationMissingException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class OfferAcceptanceSession {

    public static final List<Declaration> STANDARD_DECLARATIONS = List.of(
            Declaration.of("TERMS_AND_CONDITIONS", "Terms and Conditions",
                    "I agree to the loan terms and conditions as presented.", true),
            Declaration.of("PRIVACY_POLICY", "Privacy Policy",
                    "I consent to the collection and use of my personal data.", true),
            Declaration.of("CREDIT_REPORTING", "Credit Reporting Consent",
                    "I consent to credit reporting agencies being contacted.", true),
            Declaration.of("ELECTRONIC_SIGNATURE", "Electronic Signature Consent",
                    "I agree that my electronic signature is legally binding.", true),
            Declaration.of("MARKETING", "Marketing Communications",
                    "I would like to receive marketing communications.", false)
    );

    private final UUID sessionId;
    private final UUID applicationId;
    private final List<Declaration> declarations;
    private SessionStatus status;
    private final LocalDateTime createdAt;

    private OfferAcceptanceSession(UUID sessionId, UUID applicationId, List<Declaration> declarations,
                                    SessionStatus status, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.applicationId = applicationId;
        this.declarations = declarations;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static OfferAcceptanceSession create(UUID applicationId) {
        return new OfferAcceptanceSession(UUID.randomUUID(), applicationId,
                STANDARD_DECLARATIONS, SessionStatus.PENDING, LocalDateTime.now());
    }

    public static OfferAcceptanceSession reconstitute(UUID sessionId, UUID applicationId,
                                                       List<Declaration> declarations,
                                                       SessionStatus status, LocalDateTime createdAt) {
        return new OfferAcceptanceSession(sessionId, applicationId, declarations, status, createdAt);
    }

    public ESignRecord sign(Set<UUID> acceptedDeclarationIds, String ipAddress) {
        if (status == SessionStatus.SIGNED) {
            throw new AlreadySignedException(applicationId);
        }

        List<UUID> missingMandatory = declarations.stream()
                .filter(Declaration::mandatory)
                .map(Declaration::declarationId)
                .filter(id -> !acceptedDeclarationIds.contains(id))
                .toList();

        if (!missingMandatory.isEmpty()) {
            throw new MandatoryDeclarationMissingException(missingMandatory);
        }

        this.status = SessionStatus.SIGNED;
        return ESignRecord.create(applicationId, sessionId, acceptedDeclarationIds, ipAddress);
    }

    public UUID getSessionId() { return sessionId; }
    public UUID getApplicationId() { return applicationId; }
    public List<Declaration> getDeclarations() { return declarations; }
    public SessionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
