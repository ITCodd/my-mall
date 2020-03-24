package com.github.service.impl;

import com.github.AuthServerApplication;
import com.github.model.SysUser;
import com.github.service.SysUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest(classes={AuthServerApplication.class})
public class SysUserServiceImplTest {

    @Autowired
    private SysUserService sysUserService;

    @Test
    public void t1() {
        SysUser user=new SysUser();
        user.setUsername("codd");
        user.setPassword("123456");
        sysUserService.save(user);
    }

    @Test
    public void t2() {
        sysUserService.list().forEach(user-> System.out.println("user = " + user));
    }

    @Test
    public void t3() {
        UserDetails user = sysUserService.loadUserByUsername("codd");
        System.out.println("user = " + user);
    }
}