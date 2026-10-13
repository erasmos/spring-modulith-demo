package com.erasmos.springmodulithdemo.catalog;

import com.erasmos.springmodulithdemo.catalog.internal.Book;
import org.springframework.data.repository.CrudRepository;

interface BookRepository extends CrudRepository<Book, BookId> {
}
