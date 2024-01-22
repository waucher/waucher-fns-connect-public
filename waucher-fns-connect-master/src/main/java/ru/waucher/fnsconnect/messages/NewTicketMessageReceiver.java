package ru.waucher.fnsconnect.messages;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import ru.waucher.fnsconnect.services.FnsApiService;
import ru.waucher.fnsconnect.services.TicketStoreService;

@Component
public class NewTicketMessageReceiver {

    private FnsApiService fnsApiService;
    private ProcessedMessageSender processedMessageSender;
    private TicketStoreService ticketStoreService;
    private static Logger LOGGER = LoggerFactory.getLogger(NewTicketMessageReceiver.class);

    @Autowired
    public NewTicketMessageReceiver(FnsApiService fnsApiService,
                                    ProcessedMessageSender processedMessageSender,
                                    TicketStoreService ticketStoreService) {
        this.fnsApiService = fnsApiService;
        this.processedMessageSender = processedMessageSender;
        this.ticketStoreService = ticketStoreService;
    }

    @JmsListener(destination = "fnsconnect")
    public void processMessage(String messageText) {
        JSONObject message = new JSONObject(messageText);

        String qrData = message.getString("qrData");
        String userUuid = message.getString("userUUID");
        String issueUUID = message.getString("issueUUID");

        String ticketId;
        String receipt;

        try {
            ticketId = fnsApiService.getTicketIdWithRetry(qrData, 2);
            receipt = fnsApiService.getReceiptWithRetry(ticketId, 2);
        }
        catch (RuntimeException e)
        {
            LOGGER.error("can't perform message", e);
            ticketId = "error";
            receipt = "error";
        }

        JSONObject processedMessage = new JSONObject();
        processedMessage.put("userUUID", userUuid);
        processedMessage.put("issueUUID", issueUUID);
        processedMessage.put("ticketId", ticketId);
        processedMessage.put("receipt", receipt);

        JSONObject jsonTicket = new JSONObject(receipt);

        jsonTicket.put("userUUID", userUuid);
        jsonTicket.put("issueUUID", issueUUID);

        ticketStoreService.persistTicket(jsonTicket.toString());
        processedMessageSender.sendProcessedMessage(processedMessage.toString());
    }

}
