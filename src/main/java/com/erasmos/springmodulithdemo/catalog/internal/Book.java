package com.erasmos.springmodulithdemo.catalog.internal;

import com.erasmos.springmodulithdemo.catalog.BookId;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;
import org.springframework.data.annotation.Id;

@AggregateRoot
public record Book(@Id @Identity BookId id, String title) {
}
