package ru.waucher.fnsconnect.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:fnsconnect.properties")
public class FnsConnectConfigurationProperties {

    @Value("${fns.apiHostUrl}")
    String apiHostUrl;

    @Value("${fns.deviceOS}")
    String deviceOS;

    @Value("${fns.clientAppVersion}")
    String clientAppVersion;

    @Value("${fns.deviceId}")
    String deviceId;

    @Value("${fns.userAgent}")
    String userAgent;

    @Value("${fns.clientSecret}")
    String clientSecret;

    @Value("${fns.acceptLanguage}")
    String acceptLanguage;

    @Value("${fns.phoneNumber}")
    String phoneNumber;

    @Value("${fns.startRefreshToken}")
    String startRefreshToken;

    public String getStartRefreshToken() {
        return startRefreshToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getApiHostUrl() {
        return apiHostUrl;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public String getClientAppVersion() {
        return clientAppVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
