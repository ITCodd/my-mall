package com.github.auth.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SmsCodeRepository implements CodeRepository {

    /**
     * 目前内存方式实现，后期redis方式实现
     */
    private Map<String,String> map=new HashMap<>();

    public static final String SMS_CODE_KEY_PREFIX = "sms:deviceId:";

    private String codeKeyPrefixParameter = SMS_CODE_KEY_PREFIX;

    public String getCodeKeyPrefixParameter() {
        return codeKeyPrefixParameter;
    }

    public void setCodeKeyPrefixParameter(String codeKeyPrefixParameter) {
        this.codeKeyPrefixParameter = codeKeyPrefixParameter;
    }

    @Override
    public void genCode(String deviceId) {
        String code = RandomStringUtils.randomNumeric(6);
        log.info("{}:{},生成短信校验码:{}",codeKeyPrefixParameter,deviceId,code);
        save(deviceId,code);
    }

    @Override
    public void save(String deviceId, String code) {
        map.put(codeKeyPrefixParameter+":"+deviceId,code);
    }

    @Override
    public String get(String deviceId) {
        return map.get(codeKeyPrefixParameter+":"+deviceId);
    }

    @Override
    public boolean validate(String deviceId, String code) {
        String rawSmsCode = get(deviceId);
        if(StringUtils.isBlank(rawSmsCode)){
            return false;
        }
        if(StringUtils.equalsIgnoreCase(rawSmsCode,code)){
            map.remove(codeKeyPrefixParameter+":"+deviceId);
            return true;
        }
        return false;
    }


}
