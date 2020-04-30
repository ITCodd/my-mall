package com.github.Interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 手动控制时使用
 */
public class Oauth2Interceptor extends HandlerInterceptorAdapter {
    @Autowired(required=false)
    TokenStore tokenStore;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String accessToken = request.getParameter("access_token");
        if (StringUtils.isEmpty(accessToken)) {
            return false;
        }

        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(accessToken);
        if (oAuth2Authentication == null) {
            return false;
        }
        SecurityContextHolder.getContext().setAuthentication(oAuth2Authentication);
        return true;
    }
}