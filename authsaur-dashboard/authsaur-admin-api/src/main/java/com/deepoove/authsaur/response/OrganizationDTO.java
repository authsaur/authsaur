package com.deepoove.authsaur.response;

import lombok.Data;

@Data
public class OrganizationDTO {

    private String id;
    private String source;
    private String type;
    private String name;
    private String parentId;
    private String path;
    private int subOrgs;

}
