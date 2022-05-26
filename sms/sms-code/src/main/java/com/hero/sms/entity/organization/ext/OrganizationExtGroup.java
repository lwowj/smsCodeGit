package com.hero.sms.entity.organization.ext;

import com.hero.sms.entity.organization.Organization;
import lombok.Data;

@Data
public class OrganizationExtGroup extends Organization {

    private String groupIds;
    private String groupNames;

}
