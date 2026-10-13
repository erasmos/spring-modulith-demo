package com.erasmos.springmodulithdemo.catalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@ApplicationModuleTest
@DisplayName("The catalog module when")
class CatalogModuleTest {

    @Autowired
    private CatalogService catalogService;

    @Test
    @DisplayName("adding a book, it exists.")
    void addBook() {
        var bookId = catalogService.addBook("Questions");

        assertThat(catalogService.exists(bookId)).isTrue();
    }

}
