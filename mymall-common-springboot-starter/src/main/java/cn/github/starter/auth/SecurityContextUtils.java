package cn.github.starter.auth;

import cn.github.starter.pojo.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class SecurityContextUtils {

    private static TokenStore tokenStore;

    public static void setTokenStore(TokenStore tokenStore){
        SecurityContextUtils.tokenStore=tokenStore;
    }

    public static  UserInfo getCurrentUserInfo(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication instanceof OAuth2Authentication) {
                OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) authentication;
                OAuth2AuthenticationDetails oauth2AuthenticationDetails = (OAuth2AuthenticationDetails)oAuth2Authentication.getDetails();
                OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(oauth2AuthenticationDetails.getTokenValue());
                Map<String, Object> additionalInformation = oAuth2AccessToken.getAdditionalInformation();
                LinkedHashMap<String,Object> linkMap = (LinkedHashMap<String, Object>) additionalInformation.get("userInfo");
                UserInfo userInfo = UserInfo.builder().build();
                for (Map.Entry<String, Object> entry : linkMap.entrySet()) {
                    PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(UserInfo.class, entry.getKey());
                    if(propertyDescriptor!=null){
                        Method method = propertyDescriptor.getWriteMethod();
                        if(method==null){
                            continue;
                        }
                        try {
                            method.invoke(userInfo,entry.getValue());
                        } catch (Exception e) {
                            log.error(method.getName()+"写入值失败",e);
                        }
                    }
                }
                return userInfo;
            }
        }
        return null;
    }

    public static String getAccessToken(){
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        if (details instanceof OAuth2AuthenticationDetails) {
            String accessToken = ((OAuth2AuthenticationDetails) details).getTokenValue();
            return "Bearer "+accessToken;
        }
        return null;
    }

}
