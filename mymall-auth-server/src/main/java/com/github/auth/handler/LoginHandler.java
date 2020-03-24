package com.github.auth.handler;

import javax.servlet.http.HttpServletRequest;

public interface LoginHandler {



    /**
     * 登录验证码处理
     * @return
     */
    public boolean process(HttpServletRequest request);

}
