package com.hero.sms.service.organization;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.organization.OrganizationUserMenu;

/**
 * 商户用户菜单表 Service接口
 *
 * @author Administrator
 * @date 2020-03-08 00:13:15
 */
public interface IOrganizationUserMenuService extends IService<OrganizationUserMenu> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param organizationUserMenu organizationUserMenu
     * @return IPage<OrganizationUserMenu>
     */
    IPage<OrganizationUserMenu> findOrganizationUserMenus(QueryRequest request, OrganizationUserMenu organizationUserMenu);

    /**
     * 查询（所有）
     *
     * @param organizationUserMenu organizationUserMenu
     * @return List<OrganizationUserMenu>
     */
    List<OrganizationUserMenu> findOrganizationUserMenus(OrganizationUserMenu organizationUserMenu);

    /**
     * 新增
     *
     * @param organizationUserMenu organizationUserMenu
     */
    void createOrganizationUserMenu(OrganizationUserMenu organizationUserMenu);

    /**
     * 修改
     *
     * @param organizationUserMenu organizationUserMenu
     */
    void updateOrganizationUserMenu(OrganizationUserMenu organizationUserMenu);

    /**
     * 删除
     *
     * @param organizationUserMenu organizationUserMenu
     */
    void deleteOrganizationUserMenu(OrganizationUserMenu organizationUserMenu);

    /**
    * 删除
    *
    * @param organizationUserMenuIds organizationUserMenuIds
    */
    void deleteOrganizationUserMenus(String[] organizationUserMenuIds);

    /**
     * 查找用户权限集
     *
     * @param userAccount 账号
     * @return 用户权限集合
     */
    List<OrganizationUserMenu> findOrgUserPermissions(String userAccount);

    /**
     * 查找用户菜单集合
     *
     * @param userAccount 账号
     * @return 用户菜单集合
     */
    MenuTree<OrganizationUserMenu> findOrgUserMenusByAccount(String userAccount);

    /**
     * 获取菜单树
     * @param menus
     * @return
     */
    List<MenuTree<OrganizationUserMenu>> convertMenus(List<OrganizationUserMenu> menus);

    /**
     * 获取商户用户权限树
     * @param menu
     * @return
     */
    MenuTree<OrganizationUserMenu> findMenus(OrganizationUserMenu menu);

    /**
     * 批量删除
     * @param menuIds
     */
    @Transactional
    void deleteMeuns(String menuIds);
}
