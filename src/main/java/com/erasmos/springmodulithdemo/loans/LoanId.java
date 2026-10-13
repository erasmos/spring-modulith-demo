package com.erasmos.springmodulithdemo.loans;

import org.jmolecules.ddd.types.Identifier;
import org.springframework.modulith.NamedInterface;

import static java.util.Objects.requireNonNull;

@NamedInterface("ids")
public record LoanId(Long value) implements Identifier {

    public LoanId {
        requireNonNull(value, "value");
    }

    public static LoanId of(Long value) {
        return new LoanId(value);
    }

}
