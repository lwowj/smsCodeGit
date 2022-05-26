package com.hero.sms.service.impl.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.common.utils.TreeUtil;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenu;
import com.hero.sms.entity.agent.AgentMenuLimit;
import com.hero.sms.entity.organization.OrganizationUserMenu;
import com.hero.sms.mapper.agent.AgentMenuMapper;
import com.hero.sms.service.agent.IAgentMenuLimitService;
import com.hero.sms.service.agent.IAgentMenuService;

/**
 * 代理菜单表 Service实现
 *
 * @author Administrator
 * @date 2020-03-06 10:05:44
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class AgentMenuServiceImpl extends ServiceImpl<AgentMenuMapper, AgentMenu> implements IAgentMenuService {

    @Autowired
    private AgentMenuMapper agentMenuMapper;



    @Autowired
    private IAgentMenuLimitService agentMenuLimitService;

    @Override
    public IPage<AgentMenu> findAgentMenus(QueryRequest request, AgentMenu agentMenu) {
        LambdaQueryWrapper<AgentMenu> queryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(agentMenu.getAuth()))
        {
        	queryWrapper.eq(AgentMenu::getAuth, agentMenu.getAuth());
        }
        // TODO 设置查询条件
        Page<AgentMenu> page = new Page<>(request.getPageNum(), request.getPageSize());
        return this.page(page, queryWrapper);
    }

    @Override
    public List<AgentMenu> findAgentMenus(AgentMenu agentMenu) {
	    LambdaQueryWrapper<AgentMenu> queryWrapper = new LambdaQueryWrapper<>();
	    if(StringUtils.isNotBlank(agentMenu.getAuth()))
        {
        	queryWrapper.eq(AgentMenu::getAuth, agentMenu.getAuth());
        }
		// TODO 设置查询条件
		return this.baseMapper.selectList(queryWrapper);
    }

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    @Override
    public List<AgentMenu> findAgentMenusByAgentId(int agentId) {
        List<AgentMenuLimit> limits =  this.agentMenuLimitService.findAgentMenuLimitsByAgentId(agentId);
        List<Long> ids = new ArrayList<>();
        ids.add(0L);
        for (AgentMenuLimit limit : limits) {
            ids.add(limit.getMenuId());
        }
        LambdaQueryWrapper<AgentMenu> queryWrapper = new LambdaQueryWrapper<>();
        // TODO 设置查询条件
        queryWrapper.in(AgentMenu::getMenuId,ids);
        return this.baseMapper.selectList(queryWrapper);
    }


    @Override
    @Transactional
    public void createAgentMenu(AgentMenu agentMenu) {
        agentMenu.setCreateTime(new Date());
        this.setAgentMenu(agentMenu);
        this.baseMapper.insert(agentMenu);
    }

    @Override
    @Transactional
    public void updateAgentMenu(AgentMenu agentMenu) {
        this.saveOrUpdate(agentMenu);
    }

    @Override
    @Transactional
    public void deleteAgentMenu(AgentMenu agentMenu) {
        LambdaQueryWrapper<AgentMenu> wrapper = new LambdaQueryWrapper<>();
	    // TODO 设置删除条件
	    this.remove(wrapper);
	}

    @Override
    public MenuTree<AgentMenu> findMenus(AgentMenu menu) {
        QueryWrapper<AgentMenu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(menu.getMenuName())) {
            queryWrapper.lambda().like(AgentMenu::getMenuName, menu.getMenuName());
        }
        queryWrapper.lambda().orderByAsc(AgentMenu::getOrderNum);
        List<AgentMenu> agentMenus = this.baseMapper.selectList(queryWrapper);
        List<MenuTree<AgentMenu>> trees = convertMenus(agentMenus);
        return TreeUtil.buildMenuTree(trees);
    }

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    @Override
    public MenuTree<AgentMenu> findMenus(int agentId) {
        List<AgentMenu> agentMenus = findAgentMenusByAgentId(agentId);
        List<MenuTree<AgentMenu>> trees = convertMenus(agentMenus);
        return TreeUtil.buildMenuTree(trees);
    }

    private List<MenuTree<AgentMenu>> convertMenus(List<AgentMenu> menus) {
        List<MenuTree<AgentMenu>> trees = new ArrayList<>();
        menus.forEach(menu -> {
            MenuTree<AgentMenu> tree = new MenuTree<>();
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

    private void setAgentMenu(AgentMenu menu) {
        if (menu.getParentId() == null)
            menu.setParentId(AgentMenu.TOP_NODE);
        if (AgentMenu.TYPE_BUTTON.equals(menu.getType())) {
            menu.setUrl(null);
            menu.setIcon(null);
        }
    }

    @Override
    @Transactional
    public void deleteMeuns(String menuIds) {
        String[] menuIdsArray = menuIds.split(StringPool.COMMA);
        this.delete(Arrays.asList(menuIdsArray));
    }

    private void delete(List<String> menuIds) {
        List<String> list = new ArrayList<>(menuIds);
        removeByIds(menuIds);
        LambdaQueryWrapper<AgentMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AgentMenu::getParentId, menuIds);
        List<AgentMenu> menus = baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(menus)) {
            List<String> menuIdList = new ArrayList<>();
            menus.forEach(m -> menuIdList.add(String.valueOf(m.getMenuId())));
            list.addAll(menuIdList);
            this.agentMenuLimitService.remove(new QueryWrapper<AgentMenuLimit>().lambda().in(AgentMenuLimit::getMenuId, menuIds));
            this.delete(menuIdList);
        } else {
           this.agentMenuLimitService.remove(new QueryWrapper<AgentMenuLimit>().lambda().in(AgentMenuLimit::getMenuId, menuIds));
        }
    }

    /**
     * 根据登录名查询权限集
     * @param agentAccount
     * @return
     */
    @Override
    public List<AgentMenu> findAgentPermissions(String agentAccount){
        return this.agentMenuMapper.findAgentPermissions(agentAccount);
    }

    /**
     * 根据登录名查询菜单集
     * @param agentAccount
     * @return
     */
    @Override
    public List<AgentMenu> findAgentMenus(String agentAccount){
        return this.agentMenuMapper.findAgentMenus(agentAccount);
    }

    /**
     * 根据登录名查询
     * @param agentAccount
     * @return
     */
    @Override
    public MenuTree<AgentMenu> findMenus(String agentAccount) {
        List<AgentMenu> agentMenus = findAgentMenus(agentAccount);
        List<MenuTree<AgentMenu>> trees = convertMenus(agentMenus);
        return TreeUtil.buildMenuTree(trees);
    }

    /**
     * 根据登录名查询代理端
     * @param agentAccount
     * @return
     */
    @Override
    public MenuTree<AgentMenu> findMenus(String agentAccount, Agent agent) {
        List<AgentMenu> agentMenus = findAgentMenus(agentAccount);
        List<MenuTree<AgentMenu>> trees = convertMenus(agentMenus,agent);
        return TreeUtil.buildMenuTree(trees);
    }


    private List<MenuTree<AgentMenu>> convertMenus(List<AgentMenu> menus, Agent agent) {
        List<MenuTree<AgentMenu>> trees = new ArrayList<>();
        menus.forEach(menu -> {
            MenuTree<AgentMenu> tree = new MenuTree<>();
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

}
