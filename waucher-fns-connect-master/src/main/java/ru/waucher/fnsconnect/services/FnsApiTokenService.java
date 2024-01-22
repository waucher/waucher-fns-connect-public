package ru.waucher.fnsconnect.services;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.waucher.fnsconnect.configuration.FnsConnectConfigurationProperties;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
public class FnsApiTokenService {

    private final FnsConnectConfigurationProperties connectionProperties;
    private String actualRefreshToken;
    private String sessionId;
    static Logger LOGGER = LoggerFactory.getLogger(FnsApiTokenService.class);


    @Autowired
    public FnsApiTokenService(FnsConnectConfigurationProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    @PostConstruct
    public void updateRefreshTokenAndSessionId() {
        actualRefreshToken = tryToGetRefreshTokenFromFile();

        ResponseEntity<String> responseRefreshToken = getRefreshTokenAndSessionId();
        JSONObject reponse = new JSONObject(responseRefreshToken.getBody());
        sessionId = reponse.getString("sessionId");
        actualRefreshToken = reponse.getString("refresh_token");
        LOGGER.info("Refresh token updated, now is:" + actualRefreshToken);
        updateRefreshTokenInFile();
    }

    private String tryToGetRefreshTokenFromFile()
    {
        try {
            try (FileInputStream fis = new FileInputStream("previous.token")) {
                return IOUtils.toString(fis, StandardCharsets.UTF_8);
            }
            catch (IOException e)
            {
                LOGGER.error("error, when open file");
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Can't update token in file");
        }
        return null;
    }

    private void updateRefreshTokenInFile() {
        try {
            try (FileWriter writer = new FileWriter("previous.token")) {
                writer.write(actualRefreshToken);
            }
            catch (IOException e)
            {
                LOGGER.error("error, when open file");
            }
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Can't update token in file");
        }
    }

    public ResponseEntity<String> getRefreshTokenAndSessionId()
    {
        String fnsApiRefreshTokenUrl = FnsApiConsts.HTTPS_PROTOCOL +
                connectionProperties.getApiHostUrl() +
                String.format("/%s/%s/%s", FnsApiConsts.MOBILE_API_VERSION, FnsApiConsts.MOBILE_API_TYPE,
                        FnsApiConsts.USERS_REFRESH_METHOD);

        URI uri = URI.create(fnsApiRefreshTokenUrl);

        if (actualRefreshToken == null) {
            actualRefreshToken = connectionProperties.getStartRefreshToken();
        }

        JSONObject requestBody = new JSONObject();

        requestBody.put(FnsApiConsts.REFRESH_TOKEN_FIELD, actualRefreshToken);
        requestBody.put(FnsApiConsts.CLIENT_SECRET_FIELD, connectionProperties.getClientSecret());

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), getHttpHeaders());
        return restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(FnsApiConsts.HOST_HTTP_HEADER_NAME, connectionProperties.getApiHostUrl());
        headers.add(FnsApiConsts.USER_AGENT_HTTP_HEADER_NAME, connectionProperties.getUserAgent());
        headers.add(FnsApiConsts.DEVICE_ID_HTTP_HEADER_NAME, connectionProperties.getDeviceId());
        headers.add(FnsApiConsts.DEVICE_OS_HTTP_HEADER_NAME, connectionProperties.getDeviceOS());
        headers.add(FnsApiConsts.CLIENT_VERSION_HTTP_HEADER_NAME, connectionProperties.getClientAppVersion());
        headers.add(FnsApiConsts.CONNECTION_NAME_FIELD_HEADER_NAME, "keep-alive");
        headers.add(FnsApiConsts.ACCEPT_ENCODING_NAME_FIELD_HEADER_NAME, "gzip, deflate, br");
        headers.add(FnsApiConsts.ACCEPT_HTTP_HEADER_NAME, "*/*");
        headers.add(FnsApiConsts.ACCEPT_LANGUAGE_HTTP_HEADER_NAME, connectionProperties.getAcceptLanguage());
        headers.add("Content-Type", "application/json");

        return headers;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getActualRefreshToken() {
        return actualRefreshToken;
    }
}
