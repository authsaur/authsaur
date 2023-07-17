package com.deepoove.authsaur.api;


import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.authentication.CasingUserInfo;
import cn.authing.sdk.java.enums.ProtocolEnum;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.hutool.core.net.URLDecoder;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.XML;
import com.deepoove.authsaur.interceptor.AssertionHolder;
import com.deepoove.authsaur.model.CasingUser;
import com.deepoove.authsaur.model.TokenCacheModel;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.setting.SettingKey;
import com.deepoove.authsaur.setting.SettingsConf;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/api")
public class LoginOutApi {

    @Autowired
    private SettingsConf settingsConf;
    @Autowired
    private CasConfigurationProperties casProperties;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/login/cas")
    public Result<TokenCacheModel> cas(@RequestParam String ticket, @RequestParam(required = false) String service) {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppHost(casProperties.getServer().getPrefix());
        // 协议
        options.setProtocol(ProtocolEnum.CAS.getValue());
        try {
            AuthenticationClient authenticationClient = new AuthenticationClient(options);
            CasingUserInfo userInfo = authenticationClient.validateTicketV2(ticket, service, "XML");

            String token = UUID.randomUUID().toString();
            CasingUser user = new CasingUser();
            user.setUserid(userInfo.getId());
            Object name = userInfo.getAttributes().get(PrincipalAttr.NAME.getAttr());
            if (null != name) {
                user.setName(String.valueOf(name));
            }
            Object mail = userInfo.getAttributes().get(PrincipalAttr.MAIL.getAttr());
            if (null != mail) {
                user.setEmail(String.valueOf(mail));
            }
            Object phone = userInfo.getAttributes().get(PrincipalAttr.PHONE.getAttr());
            if (null != phone) {
                user.setPhone(String.valueOf(phone));
            }
            Object avatar = userInfo.getAttributes().get(PrincipalAttr.AVATAR.getAttr());
            if (null != avatar) {
                user.setAvatar(String.valueOf(avatar));
            }
            TokenCacheModel tokenCacheModel = new TokenCacheModel(user, token, ticket);
            redisTemplate.opsForValue().set(token, JSONUtil.toJsonStr(tokenCacheModel), 7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(ticket, token, 7, TimeUnit.DAYS);

            return Result.success(tokenCacheModel);

        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }

    }

    @PostMapping("/login/outLogin")
    public Result<String> outLogin(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (null != token) {
            Object str = redisTemplate.opsForValue().get(token);
            if (null != str) {
                TokenCacheModel tokenCacheModel = JSONUtil.toBean(String.valueOf(str), TokenCacheModel.class);
                redisTemplate.delete(Arrays.asList(token, tokenCacheModel.getSt()));
            }
        }
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.CONSOLE);
        if ("true".equals(setting.get(SettingKey.CONSOLE_SLO))) {
            return Result.success(casProperties.getServer().getLogoutUrl());
        }
        return Result.success();
    }

    @PostMapping("/login/logout")
    public Result<Void> backendLogout(@RequestBody String body) {
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.CONSOLE);
        if (!"true".equals(setting.get(SettingKey.CONSOLE_SLO))) return Result.success();
        String decode = URLDecoder.decode(body, StandardCharsets.UTF_8);
        String logoutRequest = decode.substring("logoutRequest=".length());
        JSONObject jsonObject = XML.toJSONObject(logoutRequest);
        String sessionIndex = jsonObject.getJSONObject("samlp:LogoutRequest").getStr("samlp:SessionIndex");
        if (null != sessionIndex) {
            List<String> list = new ArrayList<>();
            list.add(sessionIndex);
            Object token = redisTemplate.opsForValue().get(sessionIndex);
            if (null != token) {
                list.add(String.valueOf(token));
            }
            redisTemplate.delete(list);
        }
        return Result.success();
    }

    @GetMapping("/currentUser")
    public Result<CasingUser> currentUser() {
        return Result.success(AssertionHolder.getAssertion());
    }

    @GetMapping("/login/config")
    public Result<String> config() {
        return Result.success(casProperties.getServer().getLoginUrl());
    }

}

