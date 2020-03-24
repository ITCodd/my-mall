package com.github.auth.mobile;

import com.github.auth.handler.LoginHandler;
import com.github.auth.handler.SmsCodeRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@Data
@Slf4j
public class SmsCodeLoginHandler implements LoginHandler {

    public static final String SMS_CODE_KEY = "code";

    private String smsCodeParameter = SMS_CODE_KEY;


    public static final String FORM_MOBILE_KEY = "mobile";

    private String mobileParameter = FORM_MOBILE_KEY;

    @Autowired
    private SmsCodeRepository smsCodeRepository;

    @Override
    public boolean process(HttpServletRequest request) {
        String mobile = obtainMobile(request);
        String smsCode = obtainSmsCode(request);
        return smsCodeRepository.validate(mobile, smsCode);
    }

    protected String obtainSmsCode(HttpServletRequest request) {
        return StringUtils.trim(request.getParameter(smsCodeParameter));
    }

    protected String obtainMobile(HttpServletRequest request) {
        return StringUtils.trim(request.getParameter(mobileParameter));
    }

}
