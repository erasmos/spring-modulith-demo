package com.erasmos.springmodulithdemo.notifications;

import com.erasmos.springmodulithdemo.loans.BookCheckedOut;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/*
    The default mode is UPDATE, but included here explicitly.
 */
@TestPropertySource(properties = "spring.modulith.events.completion-mode=UPDATE")
@DisplayName("The notifications module (when in 'update' completion mode) when")
class NotificationsModuleUpdateCompletionModeTest extends NotificationsModuleTest {

    @Test
    @DisplayName("the email gateway fails, the publication is stored as failed, and none are completed.")
    @Override
    void emailGatewayFails(Scenario scenario) {
        givenThatEmailGatewayFails(MEMBER_ID);

        scenario.publish(new BookCheckedOut(LOAN_ID, BOOK_ID, MEMBER_ID, Instant.now())).andWaitForStateChange(() -> getFailedEventPublicationRow(BOOK_ID))
                .andVerify(eventPublicationRowOpt -> {
                    assertThat(eventPublicationRowOpt).hasValueSatisfying(eventPublicationRow ->
                            assertFailed(eventPublicationRow, 1));
                    assertNoCompletedEventPublications();
                });
        verifyEmailGatewayCalled(MEMBER_ID);
    }

    @Test
    @DisplayName("the email gateway fails, but then succeeds, resubmitting completes the same publication.")
    @Override
    void emailGatewayFailsThenSucceeds(Scenario scenario) {
        givenThatEmailGatewayFailsThenSucceeds(MEMBER_ID);

        scenario.publish(new BookCheckedOut(LOAN_ID, BOOK_ID, MEMBER_ID, Instant.now())).andWaitForStateChange(() -> getFailedEventPublicationRow(BOOK_ID))
                .andVerify(eventPublicationRowOpt -> {
                    assertThat(eventPublicationRowOpt).hasValueSatisfying(eventPublicationRow -> assertFailed(eventPublicationRow, 1));
                    assertNoCompletedEventPublications();
                });

        var failedEventPublicationRow = getExpectedEventPublicationRow(BOOK_ID);
        assertThat(failedEventPublicationRow.lastResubmissionDate()).isEqualTo(failedEventPublicationRow.publicationDate());

        scenario.stimulate(this::resubmitFailedEventPublications)
                .andWaitForStateChange(() -> getCompletedEventPublicationRow(BOOK_ID))
                .andVerify(eventPublicationRowOpt ->
                        assertThat(eventPublicationRowOpt).hasValueSatisfying(eventPublicationRow -> {
                            assertThat(eventPublicationRow.id()).isEqualTo(failedEventPublicationRow.id());
                            assertThat(eventPublicationRow.completionDate()).isNotNull();
                            assertThat(eventPublicationRow.completionAttempts()).isEqualTo(2);
                            assertThat(eventPublicationRow.lastResubmissionDate()).isAfter(failedEventPublicationRow.lastResubmissionDate());
                            assertThat(getAllCompletedEventPublications()).singleElement()
                                    .satisfies(completedEventPublication ->
                                            assertEquivalent(completedEventPublication, eventPublicationRow, BOOK_ID, MEMBER_ID)
                                    );
                        }));

        verifyEmailGatewayCalled(MEMBER_ID, 2);
    }

    @Test
    @DisplayName("everything succeeds, the publication is stored as completed.")
    @Override
    void success(Scenario scenario) {
        scenario.publish(new BookCheckedOut(LOAN_ID, BOOK_ID, MEMBER_ID, Instant.now())).andWaitForStateChange(() -> getCompletedEventPublicationRow(BOOK_ID))
                .andVerify(eventPublicationRowOpt ->
                        assertThat(eventPublicationRowOpt).hasValueSatisfying(eventPublicationRow -> {
                            assertThat(eventPublicationRow.completionDate()).isNotNull();
                            assertThat(eventPublicationRow.completionAttempts()).isEqualTo(1);
                            assertThat(getAllCompletedEventPublications()).singleElement()
                                    .satisfies(completedEventPublication ->
                                            assertEquivalent(completedEventPublication, eventPublicationRow, BOOK_ID, MEMBER_ID)
                                    );
                        }));
        verifyEmailGatewayCalled(MEMBER_ID);
    }


}
