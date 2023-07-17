package com.deepoove.authsaur.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceCmd<T> extends AbstractServiceCmd {


    protected String type;
    protected String tag;
    protected String homePage;
    protected String saml;

    /*
     * oauth2\OIDC
     */
    protected String clientId;

    protected String clientSecret;

    protected boolean showApprovalPrompt;
    /*
     * end oauth2\OIDC
     */

    /*
     * saml
     */
    protected boolean signResponses = true;
    protected boolean signAssertions;

//    protected String entityId;
//    protected String assertionConsumerServiceUrl;

    protected String spMetadataContent;
    /*end saml*/

    protected T appConfig;
}
