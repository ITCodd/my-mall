/**
 * 
 */
package com.github.config;

import com.github.pojo.Permission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

	private List<String> ignoredPrexUrl=new ArrayList<>();

	private Map<String,String> servicesPath=new HashMap<>();
    @Autowired
    private ZuulProperties prop;

    private AntPathMatcher pathMatcher=new AntPathMatcher();

	@PostConstruct
	public void init(){
		ZuulProperties.ZuulRoute zuulRoute = prop.getRoutes().get("mymall-auth-server");
		System.out.println("zuulRoute.getServiceId() = " + zuulRoute.getServiceId());
		System.out.println("zuulRoute.getMenu() = " + zuulRoute.getPath());
		ignoredPrexUrl.add(zuulRoute.getPath());
		for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : prop.getRoutes().entrySet()) {
			ZuulProperties.ZuulRoute route = entry.getValue();
			servicesPath.put(route.getServiceId(),route.getPath());
		}
	}

	public Permission findService(String url, Authentication authentication){
        Permission permission = Permission.builder().access(false).build();
        for (Map.Entry<String, String> entry : servicesPath.entrySet()) {
			String serviceId = entry.getKey();
			String path = entry.getValue();
            boolean match = pathMatcher.match(path, url);
            if(match){
                permission.setServiceId(serviceId);
                permission.setUrl(url);
                String menuAdmin = pathMatcher.extractPathWithinPattern(path, url);
                permission.setMenu(menuAdmin);
                if(StringUtils.equals(menuAdmin,"orders/t2")){
                    permission.setAccess(false);
                }else{
                    permission.setAccess(true);
                }
                log.info("正在访问服务：{}，访问前缀：{}",serviceId,url);
                if(justAdmin(authentication)){
                    log.info("menuAdmin = {}" + menuAdmin);
                    if(StringUtils.equals(menuAdmin,"orders/t2")){
                        permission.setAccess(true);
                    }
                }
                return permission;
            }
		}
		return permission;
	}

    private boolean justAdmin(Authentication authentication) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if(authority.getAuthority().equals("ROLE_ADMIN")){
                return true;
            }
        }
        return false;
    }


    @Override
	public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
		System.out.println("----------网关权限判断-----------------");
        String url = request.getRequestURI();
        for (String prefix : ignoredPrexUrl) {
			if(pathMatcher.match(prefix, url)){
				return true;
			}
		}
        Permission permission = findService(url,authentication);
		return permission.getAccess();
	}

}
