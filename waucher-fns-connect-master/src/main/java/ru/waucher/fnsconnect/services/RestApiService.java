package ru.waucher.fnsconnect.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestApiService {

    private FnsApiService fnsApiService;

    @Autowired
    public RestApiService(FnsApiService fnsApiService) {
        this.fnsApiService = fnsApiService;
    }

    @GetMapping("/getCheck")
    public String getCheck(String qrData)
    {
        String ticketId = fnsApiService.getTicketId(qrData);
        return fnsApiService.getReceipt(ticketId);
    }
}
