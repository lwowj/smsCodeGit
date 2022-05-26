package com.hero.sms.system.service.impl;

import com.hero.sms.system.entity.RoleMenu;
import com.hero.sms.system.mapper.RoleMenuMapper;
import com.hero.sms.system.service.IRoleMenuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Administrator
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuMapper, RoleMenu> implements IRoleMenuService {

    @Override
    @Transactional
    public void deleteRoleMenusByRoleId(List<String> roleIds) {
        this.baseMapper.delete(new QueryWrapper<RoleMenu>().lambda().in(RoleMenu::getRoleId, roleIds));
    }

    @Override
    @Transactional
    public void deleteRoleMenusByMenuId(List<String> menuIds) {
        this.baseMapper.delete(new QueryWrapper<RoleMenu>().lambda().in(RoleMenu::getMenuId, menuIds));
    }

}
