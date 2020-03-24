package com.github.auth.handler;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ValidateCodeRepository implements CodeRepository {

    @Autowired
    DefaultKaptcha defaultKaptcha;
    /**
     * 目前内存方式实现，后期redis方式实现
     */
    private Map<String,String> map=new HashMap<>();

    public static final String SMS_CODE_KEY_PREFIX = "validate:deviceId:";

    private String codeKeyPrefixParameter = SMS_CODE_KEY_PREFIX;

    public String getCodeKeyPrefixParameter() {
        return codeKeyPrefixParameter;
    }

    public void setCodeKeyPrefixParameter(String codeKeyPrefixParameter) {
        this.codeKeyPrefixParameter = codeKeyPrefixParameter;
    }

    @Override
    public void genCode(String deviceId) {
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        try {
            //生产验证码字符串并保存到session中
            String code = defaultKaptcha.createText();
            //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = defaultKaptcha.createImage(code);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            ServletOutputStream responseOutputStream =
                    response.getOutputStream();
            responseOutputStream.write(jpegOutputStream.toByteArray());
            responseOutputStream.flush();
            responseOutputStream.close();
            save(deviceId,code);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

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
