package com.hero.sms.common.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hero.sms.common.entity.FebsConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class BaseController {

    protected Map<String, Object> getDataTable(IPage<?> pageInfo) {
        return getDataTable(pageInfo, FebsConstant.DATA_MAP_INITIAL_CAPACITY);
    }

    protected Map<String, Object> getDataTable(IPage<?> pageInfo, int dataMapInitialCapacity) {
        Map<String, Object> data = new HashMap<>(dataMapInitialCapacity);
        data.put("rows", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        return data;
    }

}
