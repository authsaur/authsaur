package com.deepoove.authsaur.response;

import lombok.Data;

@Data
public class UserStrategyDTO {

    private String principal;
    private String name;
    private String action;
}
