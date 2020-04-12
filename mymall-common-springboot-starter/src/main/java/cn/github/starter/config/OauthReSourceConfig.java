package cn.github.starter.config;

import cn.github.starter.auth.SecurityContextRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;


public class OauthReSourceConfig {

    @Bean
    @ConditionalOnMissingBean(value = {SecurityContextRunner.class})
    public SecurityContextRunner securityContextUtils() { // 方法名即 Bean 名称
        return new SecurityContextRunner();
    }

}
