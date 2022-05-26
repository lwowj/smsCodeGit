package com.hero.sms.entity.organization.ext;

import java.io.Serializable;

import com.hero.sms.entity.organization.Organization;
import com.hero.sms.entity.organization.OrganizationUser;

import lombok.Data;

@Data
public class OrganizationUserExt extends OrganizationUser implements Serializable {

    private static final long serialVersionUID = 5918110316123687008L;
    private Organization organization;
}
