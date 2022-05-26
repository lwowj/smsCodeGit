package com.hero.sms.service.agent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hero.sms.common.entity.QueryRequest;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.agent.ext.AgentExt;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 商户代理 Service接口
 *
 * @author Administrator
 * @date 2020-03-06 10:05:11
 */
public interface IAgentService extends IService<Agent> {
    /**
     * 查询（分页）
     *
     * @param request QueryRequest
     * @param agent agent
     * @return IPage<Agent>
     */
    IPage<Agent> findAgents(QueryRequest request, Agent agent);

    /**
     * 查询（所有）
     *
     * @param agent agent
     * @return List<Agent>
     */
    List<Agent> findAgents(Agent agent);

    /**
     * 新增
     *
     * @param agent agent
     */
    void createAgent(Agent agent,String checkPassword);

    /**
     * 检验篡改码
     * @param agent
     * @return
     */
    boolean checkDataMd5(Agent agent);

    /**
     * 代理端修改代理
     * @param agent
     * @param upAgent
     */
    @Transactional
    void updateAgent(Agent agent, Agent upAgent);

    /**
     * 修改
     *
     * @param agent agent
     */
    void updateAgent(Agent agent);

    /**
     * 删除
     *
     * @param agent agent
     */
    void deleteAgent(Agent agent);

    /**
     * 批量删除
     * @param agentIds
     */
    @Transactional
    void deleteAgents(String[] agentIds);

    /**
     * 批量重置密码
     * @param agentAccounts
     */
    @Transactional
    void resetPasswordAgents(String[] agentAccounts,Agent upAgent);

    /**
     * 更新登录时间
     * @param username
     */
    @Transactional
    void updateLoginTime(String username, String ip);

    /**
     * 根据代理用户名查询
     * @param agentAccount
     * @return
     */
    @Transactional
    Agent findByAccount(String agentAccount);

    /**
     * 根据ID 级联查询代理及费率信息
     * @param id
     * @return
     */
    public AgentExt queryAgentExtById(Integer id);

    /**
     * 检查域名是否符合配置
     * @param domainName
     * @param agentAccount
     */
    void checkDomainName(String domainName, String agentAccount);

    /**
     * 检验谷歌验证码
     * @param username
     * @param verifyCode
     */
    void checkGoogleKey(String username, String verifyCode);

    /**
     * 绑定谷歌
     * @param agent
     * @param goologoVerifyCode
     * @param googleKey
     */
    void bindGoogle(Agent agent, String goologoVerifyCode, String googleKey,String payPassword);

    /**
     * 清除代理谷歌Key
     * @param agent
     */
    void removeGoogleKey(Agent agent, String goologoVerifyCode,String payPassword);

    /**
     * 计算md5防篡改码
     * @param agent
     * @return
     */
    public String getDataMd5(Agent agent);

    /**
     * for update 排它锁查询数据
     * @param agentId
     * @return
     */
    Agent queryOrgByIdForUpdate(Integer agentId);

    /**
     * 修改密码
     * @param agentAccount
     * @param password
     */
    @Transactional
    void updatePassword(String agentAccount, String password);

    /**
     * 修改支付密码
     * @param agentAccount
     * @param password
     */
    @Transactional
    void updatePayPassword(String agentAccount, String password);

    /**
     *  代理统计
     * @param agent
     * @return
     */
    @Transactional
    String statisticalOnAgent(Agent agent);

    /**
     * 获取代理缓存
     * @return
     */
    List<Map<Integer,String>> getAgents();

    /**
     * 获取上级代理缓存
     * @return
     */
    List<Map<Integer,String>> getUpAgents();

    /**
     * 获取当前代理及下级代理缓存
     * @return
     */
    List<Map<Integer,String>> getCurrentAgents(Agent current);
}
