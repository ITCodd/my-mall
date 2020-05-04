package com.github.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * 认证成功回调的方法
 * @author yuxuan
 *
 */
@Component
@Slf4j
public class ApplicationListenerAuthencationSuccess implements ApplicationListener<AuthenticationSuccessEvent> {

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		log.info("event.getSource(),{}",event);
		//在这里做登录日志
//		System.out.println(event.getSource()+"++++++++++++++");	
	}

}