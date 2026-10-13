package com.erasmos.springmodulithdemo.notifications;

import com.erasmos.springmodulithdemo.loans.BookCheckedOut;
import com.erasmos.springmodulithdemo.notifications.internal.EmailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final EmailGateway emailGateway;

    public NotificationService(EmailGateway emailGateway) {
        this.emailGateway = emailGateway;
    }

    /*
        This will be triggered only after the publishing transaction has been committed.

        It will run in a new transaction and is capable of being run asynchronously.
     */
    @ApplicationModuleListener
    void on(BookCheckedOut bookCheckedOut) {
        logger.info("Notifying member {} that book {} was checked out (loan {})",
                bookCheckedOut.memberId(),
                bookCheckedOut.bookId(),
                bookCheckedOut.loanId());
        emailGateway.send(
                bookCheckedOut.memberId(),
                "Book [%d] was checked out to member [%d] as loan [%d]."
                        .formatted(bookCheckedOut.bookId().value(),
                                bookCheckedOut.memberId().value(),
                                bookCheckedOut.loanId().value()));
    }
}
