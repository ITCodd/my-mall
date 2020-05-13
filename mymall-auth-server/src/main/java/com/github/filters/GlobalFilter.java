package com.github.filters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.OAuth2ClientProperties;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class GlobalFilter implements Filter {

    @Autowired
    private OAuth2ClientProperties prop;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String client_id = request.getParameter("client_id");
        String client_secret = request.getParameter("client_secret");
        if(StringUtils.isBlank(client_id)&&StringUtils.isBlank(client_secret)){
            ServletRequest proxy_req = (ServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                    //判断是否是调用getParameter
                    if (method.getName().equals("getParameterMap")) {
                        //是要增强返回值
                        // Map<String,String[]> map =(Map<String, String[]>) method.invoke(req, args);原来方式
                        //更改之后
                        Map<String, String[]> map = new HashMap<String,String[]>((Map<String,String[]>) method.invoke(request, args));
                        map.put("client_id", new String[]{prop.getClientId()});
                        map.put("client_secret", new String[]{prop.getClientSecret()});
                        return map;
                    }else if (method.getName().equals("getParameter")){
                        if(StringUtils.equals("client_id",String.valueOf(args[0]))){
                            return prop.getClientId();
                        }
                        if(StringUtils.equals("client_secret",String.valueOf(args[0]))){
                            return prop.getClientSecret();
                        }
                        return method.invoke(request,args);
                    }
                    else if (method.getName().equals("getParameterValues")){
                        if("client_id".equals(args[0])){
                            return new String[]{prop.getClientId()};
                        }
                        if("client_secret".equals(args[0])){
                            return new String[]{prop.getClientSecret()};
                        }
                        return method.invoke(request,args);
                    }
                    return method.invoke(request,args);
                }
            });

            filterChain.doFilter(proxy_req, servletResponse);
        }else{
            filterChain.doFilter(request, servletResponse);
        }

    }

    @Override
    public void destroy() {

    }
}