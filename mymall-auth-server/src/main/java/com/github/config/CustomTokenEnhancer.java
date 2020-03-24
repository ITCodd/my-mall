package com.github.config;

import com.github.model.SysUser;
import com.github.pojo.UserInfo;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;


public class CustomTokenEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
        SysUser user = (SysUser) oAuth2Authentication.getPrincipal();
        UserInfo userInfo = UserInfo.builder().id(user.getId()).status(user.getStatus())
                            .username(user.getUsername()).build();
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("userInfo", userInfo);
        ((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(additionalInfo);
        return oAuth2AccessToken;

    }
}
