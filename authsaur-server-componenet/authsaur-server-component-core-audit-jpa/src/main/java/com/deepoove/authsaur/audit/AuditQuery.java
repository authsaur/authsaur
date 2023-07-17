package com.deepoove.authsaur.audit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditQuery {

    private String principalId;
    private String ip;
    private String startDate;
    private String endDate;
    private int current;
    private int pageSize;
}
