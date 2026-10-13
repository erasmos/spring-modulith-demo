package com.erasmos.springmodulithdemo;


import com.erasmos.springmodulithdemo.catalog.BookId;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EventPublicationTestRepository {

    private final JdbcClient jdbcClient;


    public EventPublicationTestRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long getCount() {
        return jdbcClient.sql(
                """
                        SELECT COUNT(*) FROM event_publication
                        """
        ).query(Long.class).single();
    }

    public List<EventPublicationRow> findAll() {
        return jdbcClient.sql(
                """
                    SELECT id,
                       listener_id,
                       event_type,
                       serialized_event,
                       publication_date,
                       completion_date,
                       status,
                       completion_attempts,
                       last_resubmission_date
                FROM event_publication
                ORDER BY publication_date
                """
        ).query(EventPublicationRow.class).list();
    }

    public Optional<EventPublicationRow> find(String listenerId, BookId bookId) {
        return jdbcClient.sql(
                """
                        SELECT id,
                               listener_id,
                               event_type,
                               serialized_event,
                               publication_date,
                               completion_date,
                               status,
                               completion_attempts,
                               last_resubmission_date
                        FROM event_publication
                        WHERE listener_id LIKE :listenerId
                          AND serialized_event LIKE :bookId
                        """
                ).param("listenerId", "%" + listenerId + "%")
                .param("bookId", "%\"bookId\":{\"value\":" + bookId.value() + "}%")
                .query(EventPublicationRow.class).optional();
    }

    public void deleteAll() {
        jdbcClient.sql("""
                        DELETE FROM event_publication
                        """)
                .update();
    }

}
