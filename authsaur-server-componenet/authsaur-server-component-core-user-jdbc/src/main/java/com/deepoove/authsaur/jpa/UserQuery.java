package com.deepoove.authsaur.jpa;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQuery {

    private String orgId;
    private String name;
    private String email;
    private String phone;
    private int current;
    private int pageSize;
}
