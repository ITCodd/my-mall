package com.github.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.model.SysRole;

import java.util.List;

public interface RoleMapper extends BaseMapper<SysRole> {

    public List<SysRole> findRoleByUserId(Integer userId);

}
