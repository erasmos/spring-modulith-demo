package com.erasmos.springmodulithdemo.loans;

import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BookCheckedOutListener {

    private final List<BookCheckedOut> receivedImmediately = new CopyOnWriteArrayList<>();
    private final List<BookCheckedOut> receivedAfterCommit = new CopyOnWriteArrayList<>();

    @EventListener
    void onImmediately(BookCheckedOut bookCheckedOut) {
        receivedImmediately.add(bookCheckedOut);
    }

    @ApplicationModuleListener
    void onAfterCommit(BookCheckedOut bookCheckedOut) {
        receivedAfterCommit.add(bookCheckedOut);
    }

    public List<BookCheckedOut> getReceivedImmediately() {
        return receivedImmediately;
    }

    public List<BookCheckedOut> getReceivedAfterCommit() {
        return receivedAfterCommit;
    }

}
