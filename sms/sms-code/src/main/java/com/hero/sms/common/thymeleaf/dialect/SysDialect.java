package com.hero.sms.common.thymeleaf.dialect;

import java.util.HashSet;
import java.util.Set;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.processor.StandardXmlNsTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import com.hero.sms.common.thymeleaf.tagProcessor.SysDataTagProcessor;
import com.hero.sms.common.thymeleaf.tagProcessor.SysDictTagProcessor;
import com.hero.sms.common.thymeleaf.tagProcessor.SysTemplateTagProcessor;

public class SysDialect extends AbstractProcessorDialect {
    // 定义方言名称
    private static final String DIALECT_NAME = "Sys Dialect";
    public SysDialect() {
        // 设置自定义方言与“方言处理器”优先级相同
        super(DIALECT_NAME, "sys", StandardDialect.PROCESSOR_PRECEDENCE);
    }


    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        Set<IProcessor> processors = new HashSet<IProcessor>();

        // 添加自定义标签
        processors.add(new SysDictTagProcessor(dialectPrefix));
        processors.add(new SysDataTagProcessor(dialectPrefix));
        processors.add(new SysTemplateTagProcessor(dialectPrefix));
        processors.add(new StandardXmlNsTagProcessor(TemplateMode.HTML, dialectPrefix));
        return processors;
    }
}
