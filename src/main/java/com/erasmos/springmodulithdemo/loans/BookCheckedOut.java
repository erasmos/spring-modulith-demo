package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.jmolecules.event.annotation.DomainEvent;

import java.time.Instant;

@DomainEvent
public record BookCheckedOut(LoanId loanId, BookId bookId, MemberId memberId, Instant checkedOutAt) {
}
