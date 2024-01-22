package ru.waucher.fnsconnect.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class TicketStoreService {

    private MongoTemplate mongoTemplate;

    @Autowired
    public TicketStoreService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void persistTicket(String ticket)
    {
        mongoTemplate.save(ticket, "tickets");
    }
}
