package com.erasmos.springmodulithdemo.catalog;

import org.jmolecules.ddd.types.Identifier;
import org.springframework.modulith.NamedInterface;

import static java.util.Objects.requireNonNull;

@NamedInterface("ids")
public record BookId(Long value) implements Identifier {

    public BookId {
        requireNonNull(value, "value");
    }

    public static BookId of(Long value) {
        return new BookId(value);
    }

}
