package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@AggregateRoot
public record Loan(@Id @Identity LoanId id, BookId bookId, MemberId memberId, Instant checkedOutAt) {

    static Loan checkOut(BookId bookId, MemberId memberId, Instant now) {
        return new Loan(null, bookId, memberId, now);
    }

}
