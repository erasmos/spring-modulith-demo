package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.EventPublicationTestRepository;
import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.catalog.CatalogService;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/*
    It only loads the loans module, as it shares the same package.

    It only injects the beans in that package and its subpackages. This is the default: BootstrapMode.STANDALONE.

    If you'd like to load all the modules which this module *directly* uses, then: DIRECT_DEPENDENCIES.

    And, if you'd like to extend that to all modules, direct or otherwise, use: ALL_DEPENDENCIES.
 */
@ApplicationModuleTest
@DisplayName("The loans module when")
@Import(BookCheckedOutListener.class)
class LoansModuleTest {

    @Autowired
    private LoansService loansService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private BookCheckedOutListener bookCheckedOutListener;

    @Autowired
    private EventPublicationTestRepository eventPublicationTestRepository;

    @MockitoBean
    private CatalogService mockCatalogService;

    @Nested
    @DisplayName("checking out a book when")
    class CheckingOutBook {

        @BeforeEach
        void deleteAllLoans() {
            loanRepository.deleteAll();
        }

        @Test
        @DisplayName("there is a transaction failure, no loan is saved nor event published.")
        void transactionFailure() {
            var bookId = BookId.of(300L);
            var memberId = MemberId.of(400L);
            var numLoansBefore = loanRepository.count();
            var numEventsBefore = eventPublicationTestRepository.getCount();
            givenBookExists(bookId);

            assertThatThrownBy(() ->
                    transactionTemplate.executeWithoutResult(transactionStatus -> {
                        loansService.checkOut(bookId, memberId);
                        throw new IllegalStateException("!");
                    })).isInstanceOf(IllegalStateException.class);

            assertThat(loanRepository.count()).isEqualTo(numLoansBefore);
            assertThat(eventPublicationTestRepository.getCount()).isEqualTo(numEventsBefore);
        }

        @Test
        @DisplayName("the book is unknown, the attempt is rejected.")
        void bookUnknown() {
            var bookId = BookId.of(100L);
            var memberId = MemberId.of(200L);
            var numLoansBefore = loanRepository.count();
            var numEventsBefore = eventPublicationTestRepository.getCount();
            givenBookDoesntExist(bookId);

            assertThatThrownBy(() -> loansService.checkOut(bookId, memberId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Book [100] doesn't exist.");

            assertThat(loanRepository.count()).isEqualTo(numLoansBefore);
            assertThat(eventPublicationTestRepository.getCount()).isEqualTo(numEventsBefore);
        }

        @Test
        @DisplayName("the book is out on loan, the second attempt is rejected.")
        void bookOnLoan() {
            var bookId = BookId.of(100L);
            var memberId = MemberId.of(200L);
            var numLoansBefore = loanRepository.count();
            var numEventsBefore = eventPublicationTestRepository.getCount();
            givenBookExists(bookId);

            loansService.checkOut(bookId, memberId);

            assertThatThrownBy(() -> loansService.checkOut(bookId, memberId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Book [100] isn't available.");

            assertThat(loanRepository.count()).isEqualTo(numLoansBefore + 1);
            assertThat(eventPublicationTestRepository.getCount()).isEqualTo(numEventsBefore + 1);
        }

        @Test
        @DisplayName("all provided fields are valid, the expected event is published.")
        void successful(Scenario scenario) {
            var bookId = BookId.of(100L);
            var memberId = MemberId.of(200L);
            givenBookExists(bookId);

            scenario.stimulate(() -> loansService.checkOut(bookId, memberId))
                    .andWaitForEventOfType(BookCheckedOut.class)
                    .toArriveAndVerify(bookCheckedOut -> {
                        assertThat(bookCheckedOut.loanId()).isNotNull();
                        assertThat(bookCheckedOut.bookId()).isEqualTo(bookId);
                        assertThat(bookCheckedOut.memberId()).isEqualTo(memberId);
                        assertThat(bookCheckedOut.checkedOutAt()).isNotNull();
                    });
        }
    }


    private void givenBookExists(BookId bookId) {
        when(mockCatalogService.exists(bookId)).thenReturn(true);
    }

    private void givenBookDoesntExist(BookId bookId) {
        when(mockCatalogService.exists(bookId)).thenReturn(false);
    }

}
