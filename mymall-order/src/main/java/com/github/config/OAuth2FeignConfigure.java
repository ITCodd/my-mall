//package com.github.config;
//
//import feign.RequestInterceptor;
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
//import org.springframework.security.oauth2.client.OAuth2RestTemplate;
//import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
//
//import javax.annotation.Resource;
//
//@Configuration
//public class OAuth2FeignConfigure {
//
//    /**
//     * 下面bean配置 是为了解决 feign 模式下 统一验证凭证的问题，因为服务全部被保护起来了
//     * @return
//     */
//    @Bean
//    @ConfigurationProperties(prefix = "security.oauth2.client")
//    public ClientCredentialsResourceDetails clientCredentialsResourceDetails() {
//        return new ClientCredentialsResourceDetails();
//    }
//    @Bean
//    public RequestInterceptor oauth2FeignRequestInterceptor(){
//        return new OAuth2FeignRequestInterceptor(new DefaultOAuth2ClientContext(), clientCredentialsResourceDetails());
//    }
//    @Bean
//    public OAuth2RestTemplate clientCredentialsRestTemplate() {
//        return new OAuth2RestTemplate(clientCredentialsResourceDetails());
//    }
//
//
//}
