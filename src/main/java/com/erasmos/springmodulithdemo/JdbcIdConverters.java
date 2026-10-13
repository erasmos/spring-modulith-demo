package com.erasmos.springmodulithdemo;

import com.erasmos.springmodulithdemo.catalog.BookId;
import com.erasmos.springmodulithdemo.loans.LoanId;
import com.erasmos.springmodulithdemo.members.MemberId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.util.List;

/**
 * Placed in the root, so all modules have access.
 */
@Configuration
public class JdbcIdConverters {

    @Bean
    JdbcCustomConversions jdbcCustomConversions() {
        return new JdbcCustomConversions(
                List.of(
                        new BookIdToLong(),
                        new LongToBookId(),
                        new MemberIdToLong(),
                        new LongToMemberId(),
                        new LoanIdToLong(),
                        new LongToLoanId()
                )
        );
    }

    @WritingConverter
    static class BookIdToLong implements Converter<BookId, Long> {

        @Override
        public Long convert(BookId bookId) {
            return bookId.value();
        }
    }

    @ReadingConverter
    static class LongToBookId implements Converter<Long, BookId> {

        @Override
        public BookId convert(Long value) {
            return BookId.of(value);
        }
    }

    @WritingConverter
    static class MemberIdToLong implements Converter<MemberId, Long> {

        @Override
        public Long convert(MemberId memberId) {
            return memberId.value();
        }
    }

    @ReadingConverter
    static class LongToMemberId implements Converter<Long, MemberId> {

        @Override
        public MemberId convert(Long value) {
            return MemberId.of(value);
        }
    }

    @WritingConverter
    static class LoanIdToLong implements Converter<LoanId, Long> {

        @Override
        public Long convert(LoanId loanId) {
            return loanId.value();
        }

    }

    @ReadingConverter
    static class LongToLoanId implements Converter<Long, LoanId> {

        @Override
        public LoanId convert(Long value) {
            return LoanId.of(value);
        }

    }

}
