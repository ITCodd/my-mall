package com.github.controller;

import com.github.auth.handler.SmsCodeRepository;
import com.github.auth.handler.ValidateCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code")
public class CodeController {

    @Autowired
    private SmsCodeRepository smsCodeRepository;


    @Autowired
    private ValidateCodeRepository validateCodeRepository;

    @PostMapping("/sms")
    public String sendSmsCode(String mobile){
        smsCodeRepository.genCode(mobile);
        return "短信发送成功";
    }

    @GetMapping("/image")
    public void sendValidateCode(String deviceId){
        validateCodeRepository.genCode(deviceId);
    }
}
