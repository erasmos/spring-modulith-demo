package com.erasmos.springmodulithdemo.notifications;

import com.erasmos.springmodulithdemo.EventPublicationRow;
import com.erasmos.springmodulithdemo.EventPublicationTestRepository;
import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.loans.BookCheckedOut;
import com.erasmos.springmodulithdemo.loans.LoanId;
import com.erasmos.springmodulithdemo.members.MemberId;
import com.erasmos.springmodulithdemo.notifications.internal.EmailGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.EventPublication.Status;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.modulith.events.EventPublication.Status.COMPLETED;

@ApplicationModuleTest()
public abstract class NotificationsModuleTest {

    private static final Logger logger = LoggerFactory.getLogger(NotificationsModuleTest.class);
    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();
    private static final String LISTENER_ID_SUFFIX = "NotificationService";

    protected static final LoanId LOAN_ID = LoanId.of(1L);
    protected static final BookId BOOK_ID = BookId.of(100L);
    protected static final MemberId MEMBER_ID = MemberId.of(200L);

    @Autowired
    private EventPublicationTestRepository eventPublicationTestRepository;

    @Autowired
    private CompletedEventPublications completedEventPublications;

    @Autowired
    private FailedEventPublications failedEventPublications;

    @MockitoBean
    private EmailGateway mockEmailGateway;

    @Test
    abstract void emailGatewayFails(Scenario scenario);

    @Test
    abstract void emailGatewayFailsThenSucceeds(Scenario scenario);

    @Test
    abstract void success(Scenario scenario);

    @BeforeEach
    void deleteAllEvents() {
        eventPublicationTestRepository.deleteAll();
        completedEventPublications.deletePublicationsOlderThan(Duration.ZERO);
    }


    @AfterEach
    void verifyNoUnexpectedMockInteractions() {
        verifyNoMoreInteractions(mockEmailGateway);
    }

    protected Optional<EventPublicationRow> getFailedEventPublicationRow(BookId bookId) {
        return eventPublicationTestRepository.find(LISTENER_ID_SUFFIX, bookId)
                .filter(eventPublicationRow -> eventPublicationRow.status().equals(Status.FAILED));
    }

    protected Optional<EventPublicationRow> getCompletedEventPublicationRow(BookId bookId) {
        return eventPublicationTestRepository.find(LISTENER_ID_SUFFIX, bookId)
                .filter(eventPublicationRow -> eventPublicationRow.status().equals(Status.COMPLETED));
    }

    protected Optional<EventPublicationRow> getEventPublicationRow(BookId bookId) {
        return eventPublicationTestRepository.find(LISTENER_ID_SUFFIX, bookId);
    }

    protected Collection<? extends EventPublication> getAllCompletedEventPublications() {
        return completedEventPublications.findAll();
    }

    protected void resubmitFailedEventPublications() {
        failedEventPublications.resubmit(ResubmissionOptions.defaults());
    }

    protected void givenThatEmailGatewayFails(MemberId memberId) {
        doThrow(new IllegalStateException("!")).when(mockEmailGateway).send(eq(memberId), anyString());
    }

    protected void givenThatEmailGatewayFailsThenSucceeds(MemberId memberId) {
        doThrow(new IllegalStateException("!")).doNothing().when(mockEmailGateway).send(eq(memberId), anyString());
    }

    protected void verifyEmailGatewayCalled(MemberId expectedMemberId) {
        verifyEmailGatewayCalled(expectedMemberId, 1);
    }

    protected void verifyEmailGatewayCalled(MemberId expectedMemberId, int expectedTimes) {
        verify(mockEmailGateway, times(expectedTimes)).send(eq(expectedMemberId), anyString());
    }

    protected void assertFailed(EventPublicationRow eventPublicationRow, long expectedCompletionAttempts) {
        print(eventPublicationRow);
        assertThat(eventPublicationRow.completionDate()).isNull();
        assertThat(eventPublicationRow.completionAttempts()).isEqualTo(expectedCompletionAttempts);
    }

    protected void assertNoCompletedEventPublications() {
        assertThat(completedEventPublications.findAll()).isEmpty();
    }

    protected void assertEquivalent(EventPublication eventPublication, EventPublicationRow eventPublicationRow, BookId expectedBookId, MemberId expectedMemberId) {
        assertThat(eventPublication.getIdentifier()).isEqualTo(eventPublicationRow.id());
        assertThat(eventPublication.getCompletionAttempts()).isEqualTo(eventPublicationRow.completionAttempts());
        assertThat(eventPublication.getPublicationDate()).isEqualTo(eventPublicationRow.publicationDate());
        assertThat(eventPublication.getCompletionDate()).contains(eventPublicationRow.completionDate());
        assertThat(eventPublication.getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(eventPublication.getEvent()).isInstanceOfSatisfying(BookCheckedOut.class, bookCheckedOut -> {
            assertThat(bookCheckedOut.bookId()).isEqualTo(expectedBookId);
            assertThat(bookCheckedOut.memberId()).isEqualTo(expectedMemberId);
        });
    }

    protected void assertCompleted(EventPublication eventPublication, BookId expectedBookId, MemberId expectedMemberId, long expectedCompletionAttempts) {
        assertThat(eventPublication.getStatus()).isEqualTo(COMPLETED);
        assertThat(eventPublication.getIdentifier()).isNotNull();
        assertThat(eventPublication.getCompletionAttempts()).isEqualTo(expectedCompletionAttempts);
        assertThat(eventPublication.getPublicationDate()).isNotNull();
        assertThat(eventPublication.getCompletionDate()).isNotEmpty();
        assertThat(eventPublication.getEvent()).isInstanceOfSatisfying(BookCheckedOut.class, bookCheckedOut -> {
            assertThat(bookCheckedOut.bookId()).isEqualTo(expectedBookId);
            assertThat(bookCheckedOut.memberId()).isEqualTo(expectedMemberId);
        });
    }

    protected EventPublicationRow getExpectedEventPublicationRow(BookId bookId) {
        return eventPublicationTestRepository.find(LISTENER_ID_SUFFIX, bookId).orElseThrow();
    }

    protected void print(EventPublicationRow eventPublicationRow) {
        logger.info("\n{}", jsonMapper.writeValueAsString(eventPublicationRow));
    }

}
