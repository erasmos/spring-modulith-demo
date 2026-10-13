package com.erasmos.springmodulithdemo;

import com.erasmos.springmodulithdemo.catalog.CatalogService;
import com.erasmos.springmodulithdemo.loans.LoansService;
import com.erasmos.springmodulithdemo.members.MemberId;
import com.erasmos.springmodulithdemo.notifications.internal.EmailGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.modulith.test.EnableScenarios;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@SpringBootTest
@EnableScenarios // Allows the injection of a Scenario into a test.
@DisplayName("In the library, when")
class LibraryIntegrationTests {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private LoansService loansService;

    @Autowired
    private CompletedEventPublications completedEventPublications;

    @MockitoBean
    private EmailGateway mockEmailGateway;

    @Test
    @DisplayName("a book is checked out, an email is sent to the member.")
    void checkOut(Scenario scenario) {
        var memberId = MemberId.of(200L);
        var bookId = catalogService.addBook("Spring in Action");

        scenario.stimulate(() -> loansService.checkOut(bookId, memberId))
                .andWaitForStateChange(() -> !completedEventPublications.findAll().isEmpty())
                .andVerify(__ -> Mockito.verify(mockEmailGateway).send(memberId, "Book [1] was checked out to member [200] as loan [1]."));
    }
}
