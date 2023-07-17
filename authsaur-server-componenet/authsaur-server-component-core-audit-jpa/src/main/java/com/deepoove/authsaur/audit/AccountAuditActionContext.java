package com.deepoove.authsaur.audit;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;
import ua_parser.Client;
import ua_parser.Parser;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class AccountAuditActionContext extends AuditActionContext {
    private static final long serialVersionUID = 8935451143814878214L;

    private String id;
    private String principalName;
    private String actionName;
    private boolean result = true;
    private String device;
    private String os;
    private String osVer;
    private String browser;
    private String browserVer;
    private Map<String, String> user;

    private String failCode;


    public AccountAuditActionContext(final AuditActionContext context) {
        super(context.getPrincipal(), context.getResourceOperatedUpon(), context.getActionPerformed(),
                context.getApplicationCode(), context.getWhenActionWasPerformed(), context.getClientIpAddress(),
                context.getServerIpAddress(), context.getUserAgent());
        this.id = UUID.randomUUID().toString();
        Parser uaParser = new Parser();
        Client client = uaParser.parse(getUserAgent());
        os = client.os.family;
        osVer = client.os.major + "." + client.os.minor;
        browser = client.userAgent.family;
        StringBuilder sb = new StringBuilder();
        if (null != client.userAgent.major) {
            sb.append(client.userAgent.major);
            if (null != client.userAgent.minor) {
                sb.append(".").append(client.userAgent.minor);
            }
            browserVer = sb.toString();
        }
        device = client.device.family;

        String resourceOperatedUpon = this.getResourceOperatedUpon();
        switch (context.getActionPerformed()) {
            case "AUTHENTICATION_SUCCESS":
                actionName = "登录";
                if (resourceOperatedUpon.contains("TokenCredential")) {
                    actionName = "验证OTP";
                }
                break;
            case "TICKET_DESTROYED":
                if (resourceOperatedUpon.contains("TGT")) {
                    actionName = "登出";
                } else if (resourceOperatedUpon.contains("OC")) {
                    // OAuth20Code.PREFIX
                    actionName = "注销OC";
                } else if (resourceOperatedUpon.contains("TST")) {
                    // OAuth20Code.PREFIX
                    actionName = "注销TST";
                }
                break;
            case "OAUTH2_CODE_RESPONSE_CREATED":
                actionName = "创建OAuthCode";
                break;
            case "AUTHENTICATION_FAILED":
                actionName = "登录";
                result = false;
                break;
            case "TRUSTED_AUTHENTICATION_CREATED":
                actionName = "新增可信设备";
                break;
            default:
                actionName = context.getActionPerformed();
        }

        if (null != resourceOperatedUpon && context.getActionPerformed().startsWith("AUTHENTICATION_")) {
            try {
                Gson gson = new Gson();
                Map map = gson.fromJson(resourceOperatedUpon, Map.class);
                Object failCode1 = map.get("failCode");
                if (null != failCode1) {
                    this.failCode = failCode1.toString();
                }
                Object user1 = map.get("user");
                if (null != user1) {
                    if (user1 instanceof Map) {
                        this.user = (Map<String, String>) user1;
                    } else {
                        this.user = gson.fromJson(user1.toString(), Map.class);
                    }
                }
            } catch (Exception ignore) {
            }
        }


    }

}
