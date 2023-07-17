package com.deepoove.authsaur.request;

import lombok.Data;

@Data
public class OrganizationCmd {

    private String id;

    private String name;
    private String parentId;
}
