package com.github.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.mapper.SysUserMapper;
import com.github.model.SysRole;
import com.github.model.SysUser;
import com.github.service.RoleService;
import com.github.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl  extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private RoleService roleService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        QueryWrapper<SysUser> query=new QueryWrapper<>();
        query.eq("username",userName);
        SysUser user = this.getOne(query);
        List<SysRole> roles = roleService.findRoleByUserId(user.getId());
        user.setRoles(roles);
        return user;
    }

    @Override
    public UserDetails loadUserByMobile(String mobile) {
        QueryWrapper<SysUser> query=new QueryWrapper<>();
        query.eq("mobile",mobile);
        SysUser user = this.getOne(query);
        List<SysRole> roles = roleService.findRoleByUserId(user.getId());
        user.setRoles(roles);
        return user;
    }
}
