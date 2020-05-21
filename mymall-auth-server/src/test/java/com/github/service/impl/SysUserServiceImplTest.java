package com.github.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.AuthServerApplication;
import com.github.model.SysUser;
import com.github.service.SysUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
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

    @Test
    public void t4() {
        Jwt jwt = JwtHelper.decode("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mbyI6eyJnZW5kZXIiOiIwIiwib3BlbklkIjoiNDliZmJhMmMyMDNkNDZjZDlhOTkyMjg5NTE1YjE3NTEiLCJjaGFubmVsIjoicGMiLCJtb2JpbGUiOiIxODg4ODg4ODgxNCIsImF2YXRhciI6IiIsInN5cyI6Imd6ZyIsInVzZXJOYW1lIjoi6ZmI54Gr5Lic5LiT55SoIn0sImNsaWVudElkIjoiYm9uYWRlIiwiYXRpIjoiZDFmM2M1MmItZjUzNS00NTRlLWIwMDgtYjBmNGI0ZGU0MmUzIiwianRpIjoiZjhlMDJmMjYtYTJkYS00NTU5LWI3OWItMDUxMWFiZjY3YTNlIiwidXNlcktleSI6IjE4ODg4ODg4ODE0I3BjI2d6ZyNkMWYzYzUyYi1mNTM1LTQ1NGUtYjAwOC1iMGY0YjRkZTQyZTMifQ.T2O77M7CGZ0jQvl_p4z9sGIgjeZ7AbpOmgrza2G0q_8");
        JSONObject jsonObject = JSONObject.parseObject(jwt.getClaims());
        System.out.println("jsonObject = " + jsonObject.toJSONString());
    }
}