package com.github.auth.validatecode;

import com.github.auth.handler.LoginHandler;
import com.github.auth.handler.ValidateCodeRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@Data
@Slf4j
public class ValidateCodeLoginHandler implements LoginHandler {

    public static final String VALIDATE_CODE_KEY = "code";

    private String validateCodeParameter = VALIDATE_CODE_KEY;


    public static final String DEVICEID_KEY = "deviceId";

    private String deviceIdParameter = DEVICEID_KEY;

    @Autowired
    private ValidateCodeRepository validateCodeRepository;



    @Override
    public boolean process(HttpServletRequest request) {
        String deviceId = obtainDeviceId(request);
        String validateCode = obtainValidateCode(request);
        return validateCodeRepository.validate(deviceId, validateCode);
    }

    protected String obtainValidateCode(HttpServletRequest request) {
        return StringUtils.trim(request.getParameter(validateCodeParameter));
    }

    protected String obtainDeviceId(HttpServletRequest request) {
        return StringUtils.trim(request.getParameter(deviceIdParameter));
    }

}
