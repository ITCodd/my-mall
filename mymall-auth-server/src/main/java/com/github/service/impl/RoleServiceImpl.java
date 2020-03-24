package com.github.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.mapper.RoleMapper;
import com.github.model.SysRole;
import com.github.service.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl  extends ServiceImpl<RoleMapper, SysRole> implements RoleService {
    @Override
    public List<SysRole> findRoleByUserId(Integer userId) {
        return this.baseMapper.findRoleByUserId(userId);
    }
}
