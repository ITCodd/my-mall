package com.github.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.model.SysRole;

import java.util.List;

public interface RoleService extends IService<SysRole> {

    public List<SysRole> findRoleByUserId(Integer userId);
}
