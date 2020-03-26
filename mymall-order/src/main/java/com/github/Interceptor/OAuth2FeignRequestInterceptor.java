package com.github.Interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private OAuth2ClientContext oauth2ClientContext;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessToken = null;
        try{
            // 1. Hystrix 使用 SEMAPHORE 的线程模式，可以从 OAuth2ClientContext 对象中获取 Access Token
            AccessTokenRequest r = this.oauth2ClientContext.getAccessTokenRequest();
            accessToken = r.getExistingToken().toString();
        }catch(BeanCreationException e){
            // 2. 使用 Hystrix shareSecurityContext 特性，不能获得 OAuth2ClientContext，通过 SecurityContext 获取 access token
            Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
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
