package ru.waucher.fnsconnect.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProcessedMessageSender {
    private final JmsTemplate jmsTemplate;

    @Autowired
    public ProcessedMessageSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendProcessedMessage(String message) {
        jmsTemplate.convertAndSend("waucherApi", message);
    }
}
