package com.hero.sms.common.thymeleaf.tagProcessor;

import java.util.List;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.channel.SmsChannel;
import com.hero.sms.entity.organization.Organization;

public class SysDataTagProcessor extends AbstractElementTagProcessor {

    // 标签名
    private static final String TAG_NAME = "data";

    // 优先级
    private static final int PRECEDENCE = 10000;

    public SysDataTagProcessor(String dialectPrefix) {
        super(
                // 此处理器将仅应用于HTML模式
                TemplateMode.HTML,

                // 要应用于名称的匹配前缀
                dialectPrefix,

                // 标签名称：匹配此名称的特定标签
                TAG_NAME,

                // 将标签前缀应用于标签名称
                true,

                // 无属性名称：将通过标签名称匹配
                null,

                // 没有要应用于属性名称的前缀
                false,

                // 优先(内部方言自己的优先)
                PRECEDENCE
        );
    }

    @Override
    protected void doProcess(ITemplateContext context, IProcessableElementTag tag, IElementTagStructureHandler structureHandler) {
        // 从标签读取属性值，这里的值是用来作为字典的查询参数
        String dataType = tag.getAttributeValue("dataType");
        // 提交表单时的 name
        String name = tag.getAttributeValue("name");
        // 元素的 class 样式
        String dataClass = tag.getAttributeValue("class");
        String layVerify = tag.getAttributeValue("lay-verify");
        String layFilter = tag.getAttributeValue("lay-filter");

        // 创建将替换自定义标签的 DOM 结构
        IModelFactory modelFactory = context.getModelFactory();
        IModel model = modelFactory.createModel();
        // 这里是将字典的内容拼装成一个下拉框
        model.add(modelFactory.createOpenElementTag(String.format("select lay-search name='%s' lay-verify='%s' class='%s' lay-filter='%s'", name, layVerify, dataClass,layFilter)));
        model.add(modelFactory.createOpenElementTag(String.format("<option value=\"\"></option>")));
        if("agent".equals(dataType)){
            List<Agent> agentList = DatabaseCache.getAgentList();
            for (Agent agent: agentList) {
                model.add(modelFactory.createOpenElementTag(String.format("option value='%s'",agent.getId())));
                model.add(modelFactory.createText(agent.getAgentName()));
                model.add(modelFactory.createCloseElementTag("option"));
            }
        }else if("org".equals(dataType)){
            List<Organization> orgList = DatabaseCache.getOrganizationList();
            for (Organization org: orgList) {
                model.add(modelFactory.createOpenElementTag(String.format("option value='%s'",org.getOrganizationCode())));
                model.add(modelFactory.createText(org.getOrganizationName()));
                model.add(modelFactory.createCloseElementTag("option"));
            }
        }else if("smsChannel".equals(dataType)){
            List<SmsChannel> smsChannels = DatabaseCache.getSmsChannelList();
            for (SmsChannel smsChannel: smsChannels) {
                model.add(modelFactory.createOpenElementTag(String.format("option value='%s'",smsChannel.getId())));
                model.add(modelFactory.createText(smsChannel.getName()));
                model.add(modelFactory.createCloseElementTag("option"));
            }
        }
        model.add(modelFactory.createCloseElementTag("select"));

        // 利用引擎替换整合标签
        structureHandler.replaceWith(model, false);

    }
}
