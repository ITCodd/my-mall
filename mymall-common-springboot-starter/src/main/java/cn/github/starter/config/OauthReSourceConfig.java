package cn.github.starter.config;

import cn.github.starter.auth.SecurityContextUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;


public class OauthReSourceConfig {

    @Bean
    @ConditionalOnMissingBean(value = {SecurityContextUtils.class})
    public SecurityContextUtils securityContextUtils() { // 方法名即 Bean 名称
        return new SecurityContextUtils();
    }

}
