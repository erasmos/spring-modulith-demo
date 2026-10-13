package com.erasmos.springmodulithdemo.notifications.internal;

import com.erasmos.springmodulithdemo.members.MemberId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailGateway {

    private final static Logger logger = LoggerFactory.getLogger(EmailGateway.class);

    public void send(MemberId memberId, String message) {
        logger.info("Sending email to member {}: {}", memberId.value(), message);
    }

}
