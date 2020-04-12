package com.github.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2FeignRequestInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";


    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessToken = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication!=null){
            Object details = authentication.getDetails();
            if (details instanceof OAuth2AuthenticationDetails) {
                accessToken = ((OAuth2AuthenticationDetails) details).getTokenValue();
            }
        }
        log.debug("access token: {}", accessToken);
        if(accessToken != null) {
            if (requestTemplate.headers().containsKey(AUTHORIZATION_HEADER)) {
                log.warn("The Authorization token has been already set");
            } else {
                log.debug("Constructing Header {} for Token {}", AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE);
                requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, accessToken));
            }
        }
    }
}
