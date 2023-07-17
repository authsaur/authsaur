package com.deepoove.authsaur.request;

import lombok.Data;
import org.apereo.cas.services.RegisteredService;

/**
 * https://apereo.github.io/cas/6.5.x/services/Service-Management.html
 */
@Data
public abstract class AbstractServiceCmd {

    protected long id = RegisteredService.INITIAL_IDENTIFIER_VALUE;

    protected String serviceId;
    // Required name (255 characters or less).
    protected String name;
    //    private int evaluationOrder;
    // Optional free-text description of the service. (255 characters or less)
    protected String description;

    // Optional URL to use when returning an authentication response back to applications.
    protected String redirectUrl;
    protected String logo;

//    protected List<RegisteredServiceContact> contacts;
//    protected RegisteredServiceMatchingStrategy matchingStrategy;

}
