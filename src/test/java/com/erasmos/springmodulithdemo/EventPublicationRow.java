package com.erasmos.springmodulithdemo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.modulith.events.EventPublication;

import java.time.Instant;
import java.util.UUID;

public record EventPublicationRow(
        UUID id,
        String listenerId,
        String eventType,
        @JsonRawValue String serializedEvent,
        Instant publicationDate,
        Instant completionDate,
        EventPublication.Status status,
        Integer completionAttempts,
        Instant lastResubmissionDate
) {}
