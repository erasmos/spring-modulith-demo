package com.erasmos.springmodulithdemo.loans;

import com.erasmos.springmodulithdemo.catalog.BookId;
import org.springframework.data.repository.CrudRepository;

interface LoanRepository extends CrudRepository<Loan, LoanId> {

    boolean existsByBookId(BookId bookId);

}

