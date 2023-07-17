package com.deepoove.authsaur.request;

import lombok.Data;

import java.util.List;

@Data
public class UserCmd {

    private String principal = "-1";

    private String userId;
    private String source;
    private String password;
    private String name;
    private String email;
    private String mobile;
    private boolean state;

    private List<String> orgIds;
}
