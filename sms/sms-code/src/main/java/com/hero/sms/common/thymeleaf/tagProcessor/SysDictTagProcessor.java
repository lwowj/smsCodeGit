package com.hero.sms.common.thymeleaf.tagProcessor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractElementTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import com.hero.sms.common.DatabaseCache;
import com.hero.sms.entity.common.Code;

public class SysDictTagProcessor extends AbstractElementTagProcessor {

    // 标签名
    private static final String TAG_NAME = "dict";

    // 优先级
    private static final int PRECEDENCE = 10000;

    public SysDictTagProcessor(String dialectPrefix) {
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
        String sortCode = tag.getAttributeValue("sortCode");
        String parentCode = tag.getAttributeValue("parentCode");
        // 提交表单时的 name
        String name = tag.getAttributeValue("name");
        String value = tag.getAttributeValue("value");
        // 元素的 class 样式
        String dictClass = tag.getAttributeValue("class");
        String layVerify = tag.getAttributeValue("lay-verify");

        List<Code> codeList = null;
        if(StringUtils.isNotBlank(parentCode)){
            codeList = DatabaseCache.getCodeListBySortCodeAndUpCode(sortCode,parentCode);
        }else{
            codeList = DatabaseCache.getCodeListBySortCode(sortCode);
        }
        // 创建将替换自定义标签的 DOM 结构
        IModelFactory modelFactory = context.getModelFactory();
        IModel model = modelFactory.createModel();

        // 这里是将字典的内容拼装成一个下拉框
        model.add(modelFactory.createOpenElementTag(String.format("select name='%s' lay-verify='%s' class='%s'", name, layVerify, dictClass)));
        model.add(modelFactory.createOpenElementTag(String.format("option value=\"\"")));
        for (Code c: codeList) {
            if(value!=null && value.equals(c.getCode()))
                model.add(modelFactory.createOpenElementTag(String.format("option selected value='%s'",c.getCode())));
            else
                model.add(modelFactory.createOpenElementTag(String.format("option value='%s'",c.getCode())));
            model.add(modelFactory.createText(c.getName()));
            model.add(modelFactory.createCloseElementTag("option"));
        }
        model.add(modelFactory.createCloseElementTag("select"));

        // 利用引擎替换整合标签
        structureHandler.replaceWith(model, false);

    }
}
