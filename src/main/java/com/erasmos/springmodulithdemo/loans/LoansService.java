package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.catalog.CatalogService;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class LoansService {

    private final LoanRepository loanRepository;
    private final CatalogService catalogService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    LoansService(LoanRepository loanRepository, CatalogService catalogService, ApplicationEventPublisher applicationEventPublisher, Clock clock) {
        this.loanRepository = loanRepository;
        this.catalogService = catalogService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.clock = clock;
    }

    @Transactional // Without this, @ApplicationModuleListener would never fire, as it requires a commit.
    public Loan checkOut(BookId bookId, MemberId memberId) {
        if (!catalogService.exists(bookId)) throw new IllegalStateException("Book [%d] doesn't exist.".formatted(bookId.value()));
        if (loanRepository.existsByBookId(bookId)) throw new IllegalStateException("Book [%d] isn't available.".formatted(bookId.value()));
        Loan loan = loanRepository.save(Loan.checkOut(bookId, memberId, clock.instant()));
        applicationEventPublisher.publishEvent(new BookCheckedOut(loan.id(), loan.bookId(), loan.memberId(), loan.checkedOutAt()));
        return loan;
    }
}
