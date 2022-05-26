package com.hero.sms.common.thymeleaf.tagProcessor;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.agent.Agent;
import com.hero.sms.entity.channel.PayChannel;
import com.hero.sms.entity.common.Code;
import com.hero.sms.entity.organization.Organization;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.List;

public class SysTemplateTagProcessor extends AbstractElementTagProcessor {

    // 标签名
    private static final String TAG_NAME = "template";

    // 优先级
    private static final int PRECEDENCE = 10000;

    public SysTemplateTagProcessor(String dialectPrefix) {
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
        String sortCode = tag.getAttributeValue("sortCode");
        String fieldname = tag.getAttributeValue("fieldname");
        String emptyValue = tag.getAttributeValue("emptyValue");


        // 创建将替换自定义标签的 DOM 结构
        IModelFactory modelFactory = context.getModelFactory();
        IModel model = modelFactory.createModel();

        // 这里是将字典的内容拼装成一个下拉框
        model.add(modelFactory.createText(String.format("{{#var %s = {",fieldname)));
        if("agent".equals(dataType)){
            List<Agent> agents = DatabaseCache.getAgentList();
            for (Agent agent: agents) {
                model.add(modelFactory.createText(String.format("'%s': {title: '%s'},",agent.getId(),agent.getAgentName())));
            }
        }
        if("payChannel".equals(dataType)){
            List<PayChannel> payChannels = DatabaseCache.getPayChannelList();
            for (PayChannel payChannel: payChannels) {
                model.add(modelFactory.createText(String.format("'%s': {title: '%s'},",payChannel.getId(),payChannel.getChannelName())));
            }
            model.add(modelFactory.createText(String.format("'%s': {title: '%s'},","","")));
        }
        if("org".equals(dataType)){
            List<Organization> organizations = DatabaseCache.getOrganizationList();
            for (Organization organization: organizations) {
                model.add(modelFactory.createText(String.format("'%s': {title: '%s'},",organization.getOrganizationCode(),organization.getOrganizationName())));
            }
        }else {
            List<Code> codeList = DatabaseCache.getCodeListBySortCode(sortCode);
            for (Code c: codeList) {
                model.add(modelFactory.createText(String.format("'%s': {title: '%s'},",c.getCode(),c.getName())));
            }
            //null的时候显示 ’‘
            model.add(modelFactory.createText("null : {title:''},"));
        }
        if(StringUtils.isNotEmpty(emptyValue)){
            String[] values = emptyValue.split(",");
            for (String value:values) {
                model.add(modelFactory.createText(value+" : {title:''},"));
            }
        }
        model.add(modelFactory.createText(String.format("}[d.%s];}}<span>{{%s.title }}</span>",fieldname,fieldname)));

        // 利用引擎替换整合标签
        structureHandler.replaceWith(model, false);

    }
}
