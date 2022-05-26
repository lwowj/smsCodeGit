package com.hero.sms.mapper.organization;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hero.sms.entity.organization.OrganizationUserMenu;

/**
 * 商户用户菜单表 Mapper
 *
 * @author Administrator
 * @date 2020-03-08 00:13:15
 */
public interface OrganizationUserMenuMapper extends BaseMapper<OrganizationUserMenu> {

    /**
     * 查找用户权限集
     *
     * @param userAccount 用户名
     * @return 用户权限集合
     */
    List<OrganizationUserMenu> findOrgUserPermissions(@Param("userAccount") String userAccount);

    /**
     * 查找用户菜单集合
     *
     * @param userAccount 用户名
     * @return 用户菜单集合
     */
    List<OrganizationUserMenu> findOrgUserMenus(String userAccount);

}
