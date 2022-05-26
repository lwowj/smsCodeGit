package com.hero.sms.mapper.organization;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hero.sms.entity.organization.OrganizationUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户用户 Mapper
 *
 * @author Administrator
 * @date 2020-03-07 21:36:18
 */
public interface OrganizationUserMapper extends BaseMapper<OrganizationUser> {

    IPage<OrganizationUser> selectLeftOrganizationPage(Page page,@Param(Constants.WRAPPER) Wrapper wrapper);

    List<Integer> selectIdsLeftOrganization(@Param(Constants.WRAPPER) Wrapper wrapper);

    List<OrganizationUser> selectLeftOrganization(@Param(Constants.WRAPPER) Wrapper wrapper);

}
