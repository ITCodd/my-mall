package com.github.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.model.SysUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface SysUserService extends IService<SysUser>, UserDetailsService {
    UserDetails loadUserByMobile(String mobile);
}
