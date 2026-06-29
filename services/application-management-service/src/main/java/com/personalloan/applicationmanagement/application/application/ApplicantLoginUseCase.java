package com.personalloan.applicationmanagement.application.application;

import com.personalloan.applicationmanagement.domain.application.Application;
import com.personalloan.applicationmanagement.domain.application.ApplicationStatus;
import com.personalloan.applicationmanagement.domain.application.port.ApplicationRepository;
import com.personalloan.applicationmanagement.domain.application.port.VerificationPort;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotAccessibleException;
import com.personalloan.applicationmanagement.domain.exception.ApplicationNotFoundException;
import com.personalloan.applicationmanagement.domain.exception.ApplicantVerificationFailedException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class ApplicantLoginUseCase {

    private static final Set<ApplicationStatus> TERMINAL_STATUSES = Set.of(
            ApplicationStatus.DECLINED, ApplicationStatus.FUNDED,
            ApplicationStatus.COMPLETED, ApplicationStatus.CANCELLED, ApplicationStatus.EXPIRED
    );

    private final ApplicationRepository applicationRepository;
    private final VerificationPort verificationPort;
    private final SecretKey jwtSecret;
    private final int jwtExpiryMinutes;

    public ApplicantLoginUseCase(ApplicationRepository applicationRepository,
                                  VerificationPort verificationPort,
                                  @Value("${jwt.secret}") String jwtSecret,
                                  @Value("${jwt.expiry-minutes:30}") int jwtExpiryMinutes) {
        this.applicationRepository = applicationRepository;
        this.verificationPort = verificationPort;
        this.jwtSecret = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiryMinutes = jwtExpiryMinutes;
    }

    public ApplicantLoginResult execute(UUID applicationId, String last4SSN, LocalDate dateOfBirth) {
        Application application = applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        if (TERMINAL_STATUSES.contains(application.getApplicationStatus())) {
            throw new ApplicationNotAccessibleException(applicationId);
        }

        if (!verificationPort.verify(applicationId, last4SSN, dateOfBirth)) {
            throw new ApplicantVerificationFailedException();
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(jwtExpiryMinutes);
        String token = Jwts.builder()
                .subject(applicationId.toString())
                .claim("applicationStatus", application.getApplicationStatus().name())
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(jwtSecret)
                .compact();

        return new ApplicantLoginResult(token, expiresAt, applicationId, application.getApplicationStatus().name());
    }
}
