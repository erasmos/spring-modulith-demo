package com.erasmos.springmodulithdemo.notifications;

import com.erasmos.springmodulithdemo.loans.BookCheckedOut;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "spring.modulith.events.completion-mode=DELETE")
@DisplayName("The notifications module (when in 'delete' completion mode) when")
class NotificationsModuleDeleteCompletionModeTest extends NotificationsModuleTest {

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
    @DisplayName("the email gateway fails, but then succeeds, resubmitting completes the publication and deletes it.")
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
                .andWaitForStateChange(() -> getEventPublicationRow(BOOK_ID).isEmpty())
                .andVerify(__ -> assertNoCompletedEventPublications());

        verifyEmailGatewayCalled(MEMBER_ID, 2);
    }

    @Test
    @DisplayName("everything succeeds, the publication is completed and deleted.")
    @Override
    void success(Scenario scenario) {
        scenario.publish(new BookCheckedOut(LOAN_ID, BOOK_ID, MEMBER_ID, Instant.now())).andWaitForStateChange(() -> getEventPublicationRow(BOOK_ID).isEmpty())
                .andVerify(__ -> assertNoCompletedEventPublications());

        verifyEmailGatewayCalled(MEMBER_ID);
    }


}
