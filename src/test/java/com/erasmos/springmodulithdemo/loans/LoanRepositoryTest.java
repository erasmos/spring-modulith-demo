package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.JdbcIdConverters;
import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJdbcTest
// @Configuration classes are not picked up automatically.
@Import(JdbcIdConverters.class)
@DisplayName("The loan repository when")
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Nested
    @DisplayName("saving a loan with")
    class Saving {

        @Test
        @DisplayName("all fields provided, the new loan is retrievable.")
        void allProvided() {
            var bookId = BookId.of(100L);
            var memberId = MemberId.of(200L);
            var now = Instant.parse("2026-10-20T10:00:00Z");
            var loan = Loan.checkOut(bookId, memberId, now);

            var savedLoan = loanRepository.save(loan);

            assertThat(savedLoan.id()).isNotNull();
            assertThat(loanRepository.findById(savedLoan.id())).isEqualTo(
                    Optional.of(new Loan(savedLoan.id(), bookId, memberId, now))
            );
        }
    }

}