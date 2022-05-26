package com.hero.sms.service.agent;

import java.util.List;

//import com.hero.sms.entity.system.Menu;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.MenuTree;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.AgentMenu;

/**
 * 代理菜单表 Service接口
 *
 * @author Administrator
 * @date 2020-03-06 10:05:44
 */
public interface IAgentMenuService extends IService<AgentMenu> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agentMenu agentMenu
     * @return IPage<AgentMenu>
     */
    IPage<AgentMenu> findAgentMenus(QueryRequest request, AgentMenu agentMenu);

    /**
     * 查询（所有）
     *
     * @param agentMenu agentMenu
     * @return List<AgentMenu>
     */
    List<AgentMenu> findAgentMenus(AgentMenu agentMenu);

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    List<AgentMenu> findAgentMenusByAgentId(int agentId);

    /**
     * 新增
     *
     * @param agentMenu agentMenu
     */
    void createAgentMenu(AgentMenu agentMenu);

    /**
     * 修改
     *
     * @param agentMenu agentMenu
     */
    void updateAgentMenu(AgentMenu agentMenu);

    /**
     * 删除
     *
     * @param agentMenu agentMenu
     */
    void deleteAgentMenu(AgentMenu agentMenu);

    /**
     * 获取菜单树
     * @param menu
     * @return
     */
    MenuTree<AgentMenu> findMenus(AgentMenu menu);

    /**
     * 根据代理ID查询
     * @param agentId
     * @return
     */
    MenuTree<AgentMenu> findMenus(int agentId);

    /**
     * 删除菜单
     * @param menuIds
     */
    @Transactional
    void deleteMeuns(String menuIds);

    /**
     * 根据登录名查询权限集
     * @param agentAccount
     * @return
     */
    List<AgentMenu> findAgentPermissions(String agentAccount);

    /**
     * 根据登录查询菜单集
     * @param agentAccount
     * @return
     */
    List<AgentMenu> findAgentMenus(String agentAccount);
    /**
     * 根据登录名查询
     * @param agentAccount
     * @return
     */
    MenuTree<AgentMenu> findMenus(String agentAccount);

    /**
     * 根据登录名查询代理端
     * @param agentAccount
     * @return
     */
    MenuTree<AgentMenu> findMenus(String agentAccount, Agent agent);
}
