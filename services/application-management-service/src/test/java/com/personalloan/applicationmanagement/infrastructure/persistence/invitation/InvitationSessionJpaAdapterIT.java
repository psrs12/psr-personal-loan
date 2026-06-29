package com.personalloan.applicationmanagement.infrastructure.persistence.invitation;

import com.personalloan.applicationmanagement.domain.invitation.ApplicationSource;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSession;
import com.personalloan.applicationmanagement.domain.invitation.InvitationSessionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(InvitationSessionJpaAdapter.class)
class InvitationSessionJpaAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private InvitationSessionJpaAdapter adapter;

    @Test
    void save_andFindBySessionId_returnsSession() {
        InvitationSession session = InvitationSession.create("inv-001", ApplicationSource.INVITATION, 30);

        adapter.save(session);
        Optional<InvitationSession> found = adapter.findBySessionId(session.getSessionId());

        assertThat(found).isPresent();
        assertThat(found.get().getInvitationId()).isEqualTo("inv-001");
        assertThat(found.get().getStatus()).isEqualTo(InvitationSessionStatus.VALIDATED);
        assertThat(found.get().isExpired()).isFalse();
    }

    @Test
    void save_updatedSession_persistsNewStatus() {
        InvitationSession session = InvitationSession.create("inv-002", ApplicationSource.INVITATION, 30);
        adapter.save(session);

        session.complete();
        adapter.save(session);

        Optional<InvitationSession> found = adapter.findBySessionId(session.getSessionId());
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(InvitationSessionStatus.COMPLETED);
    }
}
