package com.deepoove.authsaur.response;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {

    private long id;
    private String principal;
    private String userId;
    private String source;
    private String name;
    private String email;
    private String mobile;
    private Boolean state = false;
    private Date created;
    private Date updated;
}
