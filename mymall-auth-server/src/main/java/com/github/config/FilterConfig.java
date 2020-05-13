package com.github.config;

import com.github.filters.GlobalFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean filterRegistration(){
        // 新建过滤器注册类
        FilterRegistrationBean registration = new FilterRegistrationBean();
        // 添加自定义 过滤器
        registration.setFilter(globalFilter());
        // 设置过滤器的URL模式
        registration.addUrlPatterns("/oauth/token");
        //设置过滤器顺序
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
    @Bean
    public GlobalFilter globalFilter(){
        return new GlobalFilter();
    }
}
