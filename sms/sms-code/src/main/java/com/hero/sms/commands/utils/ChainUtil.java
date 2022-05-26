package com.hero.sms.commands.utils;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ChainUtil {

    public static final String CHAIN_CONFIG_NAME = "chain-cfg.xml";

    public static Catalog catalog;

    /**
     * 根据chainName从配置文件装配处理链条
     * @param chainName
     * @return
     * @throws Exception
     */
    public static Command getChain(String chainName) throws Exception {
        if (catalog == null){
            //获取resources下的chain-cfg.xml
            Resource resource = new ClassPathResource(CHAIN_CONFIG_NAME);
            //解析配置文件
            ConfigParser parser = new ConfigParser();
            parser.parse(resource.getURL());
            catalog = CatalogFactoryBase.getInstance().getCatalog();
        }
        //加载chain
        return catalog.getCommand(chainName);
    }
}
