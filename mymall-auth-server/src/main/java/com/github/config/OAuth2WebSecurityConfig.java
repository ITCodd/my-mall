package com.github.config;

import com.github.auth.mobile.SmsCodeAuthenticationSecurityConfig;
import com.github.auth.validatecode.ValidateCodeAuthenticationFilter;
import com.github.auth.validatecode.ValidateCodeLoginHandler;
import com.github.handler.MymallAccessDeniedHandler;
import com.github.handler.MymallAuthenticationEntryPoint;
import com.github.handler.MymallAuthenticationFailureHandler;
import com.github.handler.MymallAuthenticationSucessHandler;
import com.github.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class OAuth2WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SysUserService userService;

    @Autowired
    private SmsCodeAuthenticationSecurityConfig smsCodeAuthenticationSecurityConfig;

    @Autowired
    private ValidateCodeLoginHandler validateCodeLoginHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public MymallAuthenticationFailureHandler authenticationFailureHandler(){
        return new MymallAuthenticationFailureHandler();
    }

    @Bean
    public MymallAuthenticationSucessHandler authenticationSucessHandler(){
        return new MymallAuthenticationSucessHandler();
    }

    @Bean
    public ValidateCodeAuthenticationFilter validateCodeAuthenticationFilter() throws Exception {
        ValidateCodeAuthenticationFilter f=new ValidateCodeAuthenticationFilter();
        f.setAuthenticationSuccessHandler(authenticationSucessHandler());
        f.setAuthenticationFailureHandler(authenticationFailureHandler());
        f.setAuthenticationManager(authenticationManagerBean());
        f.setLoginHandler(validateCodeLoginHandler);
        return f;
    }

    @Bean
    public MymallAccessDeniedHandler accessDeniedHandler(){
        return new MymallAccessDeniedHandler();
    }

    @Bean
    public MymallAuthenticationEntryPoint authenticationEntryPoint(){
        return new MymallAuthenticationEntryPoint();
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //抽成smsCodeAuthenticationSecurityConfig一样的配置，校验码不起效果，顾直接应用
        http.addFilterAfter(validateCodeAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.apply(smsCodeAuthenticationSecurityConfig);
        http.authorizeRequests()
                .antMatchers("/oauth/**","/auth/**","/code/**")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginProcessingUrl("/auth/formLogin")
                .successHandler(authenticationSucessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .authenticationEntryPoint(authenticationEntryPoint());

    }


}
