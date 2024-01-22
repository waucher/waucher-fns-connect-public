package ru.waucher.fnsconnect.services;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.waucher.fnsconnect.configuration.FnsConnectConfigurationProperties;

import java.net.URI;
import java.util.Collections;

@Component
public class FnsApiService {

    private final FnsConnectConfigurationProperties connectionProperties;
    private final FnsApiTokenService tokenService;
    private static Logger LOGGER = LoggerFactory.getLogger(FnsApiService.class);

    @Autowired
    public FnsApiService(FnsConnectConfigurationProperties connectionProperties,
                         FnsApiTokenService tokenService) {
        this.connectionProperties = connectionProperties;
        this.tokenService = tokenService;
    }

    private String getFnsUrl() {
        return String.format("https://%s/%s/",connectionProperties.getApiHostUrl(), FnsApiConsts.MOBILE_API_VERSION);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.add(FnsApiConsts.HOST_HTTP_HEADER_NAME, connectionProperties.getApiHostUrl());
        headers.add(FnsApiConsts.USER_AGENT_HTTP_HEADER_NAME, connectionProperties.getUserAgent());
        headers.add(FnsApiConsts.DEVICE_ID_HTTP_HEADER_NAME, connectionProperties.getDeviceId());
        headers.add(FnsApiConsts.DEVICE_OS_HTTP_HEADER_NAME, connectionProperties.getDeviceOS());
        headers.add(FnsApiConsts.CLIENT_VERSION_HTTP_HEADER_NAME, connectionProperties.getClientAppVersion());
        headers.add(FnsApiConsts.ACCEPT_HTTP_HEADER_NAME, "*/*");
        headers.add(FnsApiConsts.ACCEPT_LANGUAGE_HTTP_HEADER_NAME, connectionProperties.getAcceptLanguage());
        headers.add("Content-Type", "application/json");
        return headers;
    }

    public String getReceipt(String ticketId) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = URI.create(getFnsUrl() + "tickets/" + ticketId);
        HttpHeaders headers = getHttpHeaders();
        headers.add(FnsApiConsts.SESSION_ID_HTTP_HEADER_NAME, tokenService.getSessionId());

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return result.getBody();
    }

    public String getReceiptWithRetry(String ticketId, Integer retryCount) {
        Integer performRetryCount = 0;
        while (performRetryCount < retryCount) {
            retryCount++;
            LOGGER.info("try to get receipt from fns, try number:" + performRetryCount);
            try {
                return getReceipt(ticketId);
            }
            catch (RuntimeException e) {
                LOGGER.error("Error when try to get receipt", e);
                tokenService.updateRefreshTokenAndSessionId();
            }
        }
        throw new RuntimeException("Can't get receipt from fns");
    }

    public String getTicketIdWithRetry(String qrData, Integer retryCount) {
        Integer performRetryCount = 0;
        while (performRetryCount < retryCount) {
            retryCount++;
            LOGGER.info("try to get receipt from fns, try number:" + performRetryCount);
            try {
                return getTicketId(qrData);
            }
            catch (RuntimeException e) {
                LOGGER.error("Error when try to get ticket id", e);
                tokenService.updateRefreshTokenAndSessionId();
            }
        }
        throw new RuntimeException("Can't get ticket id from fns");
    }

    public String getTicketId(String qrData) {
        RestTemplate restTemplate = new RestTemplate();

        URI uri = URI.create(getFnsUrl() + "ticket");
        String requestBody = String.format("{\"qr\": \"%s\"}", qrData);
        HttpHeaders headers = getHttpHeaders();
        headers.add(FnsApiConsts.ACCEPT_ENCODING_NAME_FIELD_HEADER_NAME, "gzip, deflate, br");
        headers.add(FnsApiConsts.CONNECTION_NAME_FIELD_HEADER_NAME, "keep-alive");
        headers.add(FnsApiConsts.REFRESH_TOKEN_FIELD, tokenService.getActualRefreshToken());
        headers.add(FnsApiConsts.SESSION_ID_HTTP_HEADER_NAME, tokenService.getSessionId());

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
        JSONObject reponse = new JSONObject(result.getBody());
        String ticketId = reponse.getString("id");
        LOGGER.info("Try to get ticket ID, is result:" + ticketId);
        return ticketId;
    }
}
