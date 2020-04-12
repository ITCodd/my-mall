package cn.github.starter.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.oauth2.provider.token.TokenStore;


public class SecurityContextRunner implements CommandLineRunner {
    @Autowired(required=false)
    TokenStore tokenStore;

    @Override
    public void run(String... args) throws Exception {
        SecurityContextUtils.setTokenStore(tokenStore);
    }
}
