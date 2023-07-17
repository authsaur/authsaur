package com.deepoove.authsaur.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccessStrategyDTO {

    private long appId;

    private List<UserStrategyDTO> users = new ArrayList<>();

    private List<OrgStrategyDTO> orgs = new ArrayList<>();


}
