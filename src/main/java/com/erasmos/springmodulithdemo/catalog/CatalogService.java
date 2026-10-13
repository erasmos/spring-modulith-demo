package com.erasmos.springmodulithdemo.catalog;

import com.erasmos.springmodulithdemo.catalog.internal.Book;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final BookRepository bookRepository;

    CatalogService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookId addBook(String title) {
        Book book = bookRepository.save(new Book(null, title));
        return book.id();
    }

    public boolean exists(BookId bookId) {
        return bookRepository.existsById(bookId);
    }

}
