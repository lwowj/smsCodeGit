package com.hero.sms.service.impl.organization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.TreeUtil;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.entity.organization.OrganizationUserLimit;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.mapper.organization.OrganizationUserMenuMapper;
import com.hero.sms.service.organization.IOrganizationUserLimitService;
import com.hero.sms.service.organization.IOrganizationUserMenuService;

/**
 * 商户用户菜单表 Service实现
 *
 * @author Administrator
 * @date 2020-03-08 00:13:15
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class OrganizationUserMenuServiceImpl extends ServiceImpl<OrganizationUserMenuMapper, OrganizationUserMenu> implements IOrganizationUserMenuService {

    @Autowired
    private OrganizationUserMenuMapper organizationUserMenuMapper;
    @Autowired
    private IOrganizationUserLimitService organizationUserLimitService;

    @Override
    public IPage<OrganizationUserMenu> findOrganizationUserMenus(QueryRequest request, OrganizationUserMenu organizationUserMenu) {
        LambdaQueryWrapper<OrganizationUserMenu> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        Page<OrganizationUserMenu> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<OrganizationUserMenu> findOrganizationUserMenus(OrganizationUserMenu organizationUserMenu) {
	    LambdaQueryWrapper<OrganizationUserMenu> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(organizationUserMenu.getAuth()))
        {
        	queryWrapper.eq(OrganizationUserMenu::getAuth, organizationUserMenu.getAuth());
        }
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public void createOrganizationUserMenu(OrganizationUserMenu organizationUserMenu) {
        organizationUserMenu.setCreateTime(new Date());
        this.setOrganizationUserMenu(organizationUserMenu);
        this.save(organizationUserMenu);
    }

    private void setOrganizationUserMenu(OrganizationUserMenu menu) {
        if (menu.getParentId() == null)
            menu.setParentId(AgentMenu.TOP_NODE);
        if (AgentMenu.TYPE_BUTTON.equals(menu.getType())) {
            menu.setUrl(null);
            menu.setIcon(null);
        }
    }

    @Override
    @Transactional
    public void updateOrganizationUserMenu(OrganizationUserMenu organizationUserMenu) {
        this.saveOrUpdate(organizationUserMenu);
    }

    @Override
    @Transactional
    public void deleteOrganizationUserMenu(OrganizationUserMenu organizationUserMenu) {
        LambdaQueryWrapper<OrganizationUserMenu> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    @Transactional
    public void deleteOrganizationUserMenus(String[] organizationUserMenuIds) {
        List<String> list = Arrays.asList(organizationUserMenuIds);
        this.removeByIds(list);
    }

    @Override
    public List<OrganizationUserMenu> findOrgUserPermissions(String userAccount) {

        return this.organizationUserMenuMapper.findOrgUserPermissions(userAccount);
    }

    @Override
    public MenuTree<OrganizationUserMenu> findOrgUserMenusByAccount(String userAccount) {
        List<OrganizationUserMenu> menus = this.baseMapper.findOrgUserMenus(userAccount);
        List<MenuTree<OrganizationUserMenu>> trees = this.convertMenus(menus);
        return TreeUtil.buildMenuTree(trees);
    }

    /**
     * 获取商户用户权限树
     * @param menu
     * @return
     */
    @Override
    public MenuTree<OrganizationUserMenu> findMenus(OrganizationUserMenu menu) {
        QueryWrapper<OrganizationUserMenu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(menu.getMenuName())) {
            queryWrapper.lambda().like(OrganizationUserMenu::getMenuName, menu.getMenuName());
        }
        if(StringUtils.isNotBlank(menu.getAuth()))
        {
        	queryWrapper.lambda().eq(OrganizationUserMenu::getAuth, menu.getAuth());
        }
        queryWrapper.lambda().orderByAsc(OrganizationUserMenu::getOrderNum);
        List<OrganizationUserMenu> agentMenus = this.baseMapper.selectList(queryWrapper);
        List<MenuTree<OrganizationUserMenu>> trees = convertMenus(agentMenus);
        return TreeUtil.buildMenuTree(trees);
    }

    public List<MenuTree<OrganizationUserMenu>> convertMenus(List<OrganizationUserMenu> menus) {
        List<MenuTree<OrganizationUserMenu>> trees = new ArrayList<>();
        menus.forEach(menu -> {
            MenuTree<OrganizationUserMenu> tree = new MenuTree<>();
            tree.setId(String.valueOf(menu.getMenuId()));
            tree.setParentId(String.valueOf(menu.getParentId()));
            tree.setTitle(menu.getMenuName());
            tree.setIcon(menu.getIcon());
            tree.setHref(menu.getUrl());
            tree.setData(menu);
            trees.add(tree);
        });
        return trees;
    }

    /**
     * 批量删除
     * @param menuIds
     */
    @Override
    @Transactional
    public void deleteMeuns(String menuIds) {
        String[] menuIdsArray = menuIds.split(StringPool.COMMA);
        this.delete(Arrays.asList(menuIdsArray));
    }

    private void delete(List<String> menuIds) {
        List<String> list = new ArrayList<>(menuIds);
        removeByIds(menuIds);
        LambdaQueryWrapper<OrganizationUserMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrganizationUserMenu::getParentId, menuIds);
        List<OrganizationUserMenu> menus = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(menus)) {
            List<String> menuIdList = new ArrayList<>();
            menus.forEach(m -> menuIdList.add(String.valueOf(m.getMenuId())));
            list.addAll(menuIdList);
            this.organizationUserLimitService.remove(new QueryWrapper<OrganizationUserLimit>().lambda().in(OrganizationUserLimit::getMenuId, menuIds));
            this.delete(menuIdList);
        } else {
            this.organizationUserLimitService.remove(new QueryWrapper<OrganizationUserLimit>().lambda().in(OrganizationUserLimit::getMenuId, menuIds));
        }
    }

}
