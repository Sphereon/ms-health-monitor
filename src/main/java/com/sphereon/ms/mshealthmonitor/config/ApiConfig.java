package com.sphereon.ms.mshealthmonitor.config;

import com.sphereon.libs.authentication.api.AuthenticationApi;
import com.sphereon.libs.authentication.api.TokenRequest;
import com.sphereon.libs.authentication.api.TokenRequest.TokenResponseListener;
import com.sphereon.libs.authentication.api.TokenResponse;
import com.sphereon.libs.authentication.api.config.ApiConfiguration;
import com.sphereon.libs.authentication.api.config.PersistenceMode;
import com.sphereon.libs.authentication.api.config.PersistenceType;
import com.sphereon.sdk.blockchain.easy.api.AllApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    private static Logger logger = LoggerFactory.getLogger(ApiConfig.class);

    private static final long TOKEN_VALIDITY_SECONDS = 5400;


    @Bean
    AuthenticationApi authenticationApi(@Value("${sphereon.authentication-api.application-name:ms-health-monitor}")
      String storeApplicationName) {
        ApiConfiguration tokenApiConfig = new ApiConfiguration.Builder()
          .withApplication(storeApplicationName)
          .withPersistenceType(PersistenceType.SYSTEM_ENVIRONMENT)
          .withPersistenceMode(PersistenceMode.READ_ONLY)
          .withAutoEncryptSecrets(true)
          .build();
        return new AuthenticationApi.Builder()
          .withConfiguration(tokenApiConfig).build();
    }


    @Bean
    TokenRequest tokenRequest(@Autowired AuthenticationApi authenticationApi) {
        return authenticationApi.requestToken()
          .withValidityPeriod(TOKEN_VALIDITY_SECONDS)
          .build();
    }


    @Bean
    AllApi allApi(TokenRequest tokenRequest) {
        final var allApi = new AllApi();
        tokenRequest.addTokenResponseListener(new TokenResponseListener() {
            @Override
            public void tokenResponse(final TokenResponse tokenResponse) {
                allApi.getApiClient().setAccessToken(tokenResponse.getAccessToken());
            }

            @Override
            public void exception(final Throwable throwable) {
                logger.error("Could not retrieve an access token", throwable);
            }
        });
        return allApi;
    }
}
