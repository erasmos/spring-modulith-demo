package com.erasmos.springmodulithdemo.members;

import org.jmolecules.ddd.types.Identifier;
import org.springframework.modulith.NamedInterface;

import static java.util.Objects.requireNonNull;

@NamedInterface("ids")
public record MemberId(Long value) implements Identifier {

    public MemberId {
        requireNonNull(value, "value");
    }

    public static MemberId of(Long value) {
        return new MemberId(value);
    }
}
