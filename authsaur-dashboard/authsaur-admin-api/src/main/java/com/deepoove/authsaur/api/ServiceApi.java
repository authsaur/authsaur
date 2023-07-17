package com.deepoove.authsaur.api;

import com.deepoove.authsaur.event.AuthsaurEvent;
import com.deepoove.authsaur.event.AuthsaurEventTrigger;
import com.deepoove.authsaur.jpa.JpaAppConfigEntity;
import com.deepoove.authsaur.jpa.JpaAppConfigRegistry;
import com.deepoove.authsaur.jpa.JpaAppRegistry;
import com.deepoove.authsaur.model.service.*;
import com.deepoove.authsaur.principal.PrincipalAttr;
import com.deepoove.authsaur.request.ServiceAttributeCmd;
import com.deepoove.authsaur.request.ServiceCmd;
import com.deepoove.authsaur.response.Result;
import com.deepoove.authsaur.response.ServiceAttributeDTO;
import com.deepoove.authsaur.services.RenamedAttributeReleasePolicy;
import com.deepoove.authsaur.services.ServicePropertyKey;
import com.deepoove.authsaur.services.ZentaoRegisteredService;
import com.deepoove.authsaur.util.JSONHelper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.*;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.model.TriStateBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/service")
public class ServiceApi {

    @Autowired
    @Qualifier("jpaServiceRegistry")
    private ServiceRegistry serviceRegistry;
    @Autowired
    private JpaAppRegistry jpaAppRegistry;
    @Autowired
    private JpaAppConfigRegistry jpaAppConfigRegistry;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private AuthsaurEventTrigger eventTrigger;

    @GetMapping
    public Result<Map<String, List<Map>>> query(@RequestParam(required = false) String name,
                                                @RequestParam(required = false) String type,
                                                @RequestParam(required = false) String tag,
                                                @RequestParam(required = false) String url) {
        return Result.successWithList(serviceRegistry.load().stream()
                .filter(s -> StringUtils.isBlank(name) || s.getName().toLowerCase()
                        .contains(name.toLowerCase()))
                .filter(s -> StringUtils.isBlank(url) || s.getServiceId()
                        .contains(url))
                .sorted((the, other) -> new CompareToBuilder().append(the.getEvaluationOrder(),
                                other.getEvaluationOrder())
                        .append(other.getId(),
                                the.getId())
                        .toComparison())
                .map(this::convert)
                .filter(s -> StringUtils.isBlank(type) || type.equals(s.get(
                        ServicePropertyKey.TYPE.getAttr())))
                .filter(s -> StringUtils.isBlank(tag) || (tag.equals(s.get(
                        ServicePropertyKey.TAG.getAttr()))))
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public Result<Map> get(@PathVariable Long id) {
        RegisteredService service = serviceRegistry.findServiceById(id);
        if (null == service) {
            return Result.fail("应用不存在");
        }
        Map map = convert(service);
        // plugin config
        JpaAppConfigEntity byApp = jpaAppConfigRegistry.findByApp(service.getId());
        if (null != byApp) {
            map.put("appConfig", byApp.readProperty(Map.class));
        }
        // attribute release
        ServiceAttributeDTO serviceAttributeDTO = new ServiceAttributeDTO();
        RegisteredServiceUsernameAttributeProvider usernameAttributeProvider = service.getUsernameAttributeProvider();
        if (null == usernameAttributeProvider || usernameAttributeProvider instanceof DefaultRegisteredServiceUsernameProvider) {
            serviceAttributeDTO.setPrincipalAttr("");
        } else if (usernameAttributeProvider instanceof PrincipalAttributeRegisteredServiceUsernameProvider) {
            serviceAttributeDTO.setPrincipalAttr(((PrincipalAttributeRegisteredServiceUsernameProvider) usernameAttributeProvider).getUsernameAttribute());
        }
        RegisteredServiceAttributeReleasePolicy attributeReleasePolicy = service.getAttributeReleasePolicy();
        if (attributeReleasePolicy instanceof RenamedAttributeReleasePolicy) {
            Map<String, String> renamedAttributes = ((RenamedAttributeReleasePolicy) attributeReleasePolicy).getRenamedAttributes();
            serviceAttributeDTO.setRenamedAttributes(renamedAttributes);
        }
        map.put("attributeRelease", serviceAttributeDTO);
        return Result.success(map);
    }

    private Map convert(RegisteredService service) {
        Map dto = JSONHelper.objToMap(service);
        dto.put("classType", service.getClass().getSimpleName());
        if (service instanceof SamlRegisteredService) {
            String location = ((SamlRegisteredService) service).getMetadataLocation();
            if (location.startsWith("base64://")) {
                String content = new String(Base64.decodeBase64(location.substring("base64://".length())),
                        StandardCharsets.UTF_8);
                dto.put("spMetadataContent", content);
            }
        }
        if (service instanceof OAuthRegisteredService) {
            dto.put("showApprovalPrompt", !((OAuthRegisteredService) service).isBypassApprovalPrompt());
        }
        Map<String, RegisteredServiceProperty> properties = service.getProperties();
        if (null != properties) {
            properties.entrySet().forEach(entry -> {
                RegisteredServiceProperty value = entry.getValue();
                String val = value.getValue();
                if (null != val) {
                    dto.put(entry.getKey(), val);
                }
            });
        }
        if (null == dto.get(ServicePropertyKey.TYPE.getAttr())) {
            String type = null;
            if (service.getClass().equals(RegexRegisteredService.class)) {
                type = "CAS";
            } else if (service.getClass().equals(OAuthRegisteredService.class)) {
                type = "OAuth2";
            } else if (service.getClass().equals(SamlRegisteredService.class)) {
                type = "SAML";
            } else if (service.getClass().equals(OidcRegisteredService.class)) {
                type = "Oidc";
            }
            dto.put(ServicePropertyKey.TYPE.getAttr(), type);
        }
        if (service.getLogo() != null && service.getLogo().startsWith("images")) {
            dto.put("logo", casProperties.getServer().getPrefix() + "/" + service.getLogo());
        }
        return dto;
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteService(@PathVariable Long id) {
        serviceRegistry.delete(serviceRegistry.findServiceById(id));
        jpaAppRegistry.deleteByApp(id);
        jpaAppConfigRegistry.deleteByApp(id);
        eventTrigger.trigger(AuthsaurEvent.SERVICE);
        return Result.success();
    }


    @PostMapping("/attribute")
    public Result<Void> saveOrUpdateAttributeRelease(@RequestBody ServiceAttributeCmd cmd) {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        String principalAttr = cmd.getPrincipalAttr();
        if (StringUtils.isBlank(principalAttr)) {
            service.setUsernameAttributeProvider(new DefaultRegisteredServiceUsernameProvider());
        } else {
            PrincipalAttributeRegisteredServiceUsernameProvider provider = new PrincipalAttributeRegisteredServiceUsernameProvider();
            provider.setUsernameAttribute(principalAttr);
            service.setUsernameAttributeProvider(provider);
        }

        Map<String, String> attributes = new HashMap<>();
        attributes.put(PrincipalAttr.NAME.getAttr(), cmd.getUname());
        attributes.put(PrincipalAttr.PHONE.getAttr(), cmd.getUphone());
        attributes.put(PrincipalAttr.MAIL.getAttr(), cmd.getUmail());
        RegisteredServiceAttributeReleasePolicy policy = new RenamedAttributeReleasePolicy(attributes);
        service.setAttributeReleasePolicy(policy);

        serviceRegistry.save(service);
        eventTrigger.trigger(AuthsaurEvent.SERVICE);
        return Result.success();
    }

    @PostMapping
    public Result<RegisteredService> saveOrUpdateProtocol(@RequestBody ServiceCmd<Void> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transform(cmd, service);
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateGitlab")
    public Result<RegisteredService> saveOrUpdateGitlab(@RequestBody ServiceCmd<GitlabConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformGitlab(cmd.getAppConfig(), service);
        saveService(cmd, service);

        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateJenkins")
    public Result<RegisteredService> saveOrUpdateJenkins(@RequestBody ServiceCmd<JenkinsConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformJenkins(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/securityRealm/moSamlLogin");
        }
        saveService(cmd, service);

        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateSonarqube")
    public Result<RegisteredService> saveOrUpdateSonarqube(@RequestBody ServiceCmd<SonarqubeConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformSonarqube(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/sessions/init/saml");
        }
        saveService(cmd, service);

        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateSentry")
    public Result<RegisteredService> saveOrUpdateSentry(@RequestBody ServiceCmd<SentryConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformSentry(cmd.getAppConfig(), service);
//        if (StringUtils.isBlank(cmd.getHomePage())){
//            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/securityRealm/moSamlLogin");
//        }
        saveService(cmd, service);

        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateJira")
    public Result<RegisteredService> saveOrUpdateJira(@RequestBody ServiceCmd<JiraConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformJira(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/plugins/servlet/external-login");
        }
        saveService(cmd, service);

        return Result.success(service);
    }
    @PostMapping("/saveOrUpdateConfluence")
    public Result<RegisteredService> saveOrUpdateConfluence(@RequestBody ServiceCmd<ConfluenceConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformConfluence(cmd.getAppConfig(), service);
//        if (StringUtils.isBlank(cmd.getHomePage())) {
//            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/plugins/servlet/external-login");
//        }
        saveService(cmd, service);

        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateAuthsaurAdmin")
    public Result<RegisteredService> saveOrUpdateAuthsaurAdmin(@RequestBody ServiceCmd<AuthsaurAdminConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformAuthsaurAdmin(cmd.getAppConfig(), service);
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateCodesign")
    public Result<RegisteredService> saveOrUpdateCodesign(@RequestBody ServiceCmd<CodesignConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformCodesign(cmd.getAppConfig(), service);
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateJumpServer")
    public Result<RegisteredService> saveOrUpdateJumpServer(@RequestBody ServiceCmd<JumpServerConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformJumpServer(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/core/auth/cas/login/");
        }
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateGrafana")
    public Result<RegisteredService> saveOrUpdateGrafana(@RequestBody ServiceCmd<GrafanaConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformGrafana(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl() + "/login/generic_oauth");
        }
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateGerrit")
    public Result<RegisteredService> saveOrUpdateGerrit(@RequestBody ServiceCmd<GerritConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformGerrit(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl());
        }
        saveService(cmd, service);
        return Result.success(service);
    }
    @PostMapping("/saveOrUpdateSeafile")
    public Result<RegisteredService> saveOrUpdateSeafile(@RequestBody ServiceCmd<SeafileConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformSeafile(cmd.getAppConfig(), service);
        if (StringUtils.isBlank(cmd.getHomePage())) {
            cmd.setHomePage(cmd.getAppConfig().getUrl());
        }
        saveService(cmd, service);
        return Result.success(service);
    }

    @PostMapping("/saveOrUpdateZentao")
    public Result<RegisteredService> saveOrUpdateZentao(@RequestBody ServiceCmd<ZentaoConfig> cmd) throws IOException {
        RegexRegisteredService service = (RegexRegisteredService) serviceRegistry.findServiceById(cmd.getId());
        service = transformZentao(cmd.getAppConfig(), service);
        service.setServiceId(cmd.getServiceId());

        saveService(cmd, service);
        return Result.success(service);
    }


    private static RegexRegisteredService transformZentao(ZentaoConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new ZentaoRegisteredService();
        }
        ((ZentaoRegisteredService) service).setClientId(cmd.getClientId());
        ((ZentaoRegisteredService) service).setCode(cmd.getCode());
        ((ZentaoRegisteredService) service).setSecret(cmd.getSecret());
        return service;
    }

    private static RegexRegisteredService transformGitlab(GitlabConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<?xml version=\"1.0\"?>\n" + "<md:EntityDescriptor " + "xmlns:md=\"urn:oasis" +
                ":names:tc:SAML:2.0:metadata\"\n" + "                     " + "validUntil=\"2099-06-20T11:57:29Z\"\n" + "                     cacheDuration=\"PT604800S\"\n" + "   " + "                  entityID=\"" + url + "\">\n" + "    <md:SPSSODescriptor " + "AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"true\"\n" + "                        " + "protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" + "        <md:NameIDFormat" + ">urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>\n" + "        " + "<md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" + "     " + "   <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\"\n" + "   " + "                                  Location=\"" + url + "/users/auth/saml/callback\"\n" + "          " + "                           index=\"1\"/>\n" + "    </md:SPSSODescriptor>\n" + "</md:EntityDescriptor" + ">\n";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        return service;
    }

    private static RegexRegisteredService transformJenkins(JenkinsConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<md:EntityDescriptor xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" entityID=\"" + url + "\">\n" +
                "    <md:SPSSODescriptor WantAssertionsSigned=\"true\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/securityRealm/logout\"/>\n" +
                "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"" + url + "/securityRealm/logout\"/>\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>\n" +
                "        <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/securityRealm/moSamlAuth\" index=\"1\"/>\n" +
                "    </md:SPSSODescriptor>\n" +
                "    <md:Organization>\n" +
                "        <md:OrganizationName xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xml:lang=\"en\">Xecurify</md:OrganizationName>\n" +
                "        <md:OrganizationDisplayName xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xml:lang=\"en\">Xecurify</md:OrganizationDisplayName>\n" +
                "        <md:OrganizationURL xmlns:xml=\"http://www.w3.org/XML/1998/namespace\" xml:lang=\"en\">http://miniorange.com</md:OrganizationURL>\n" +
                "    </md:Organization>\n" +
                "    <md:ContactPerson contactType=\"technical\">\n" +
                "        <md:GivenName>Xecurify</md:GivenName>\n" +
                "        <md:EmailAddress>info@xecurify.com</md:EmailAddress>\n" +
                "    </md:ContactPerson>\n" +
                "    <md:ContactPerson contactType=\"support\">\n" +
                "        <md:GivenName>Xecurify</md:GivenName>\n" +
                "        <md:EmailAddress>info@xecurify.com</md:EmailAddress>\n" +
                "    </md:ContactPerson>\n" +
                "</md:EntityDescriptor>";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        return service;
    }

    private static RegexRegisteredService transformSonarqube(SonarqubeConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + cmd.getApplicationId() + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<md:EntityDescriptor\n" +
                "    xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" validUntil=\"2099-05-19T09:47:09Z\" cacheDuration=\"PT604800S\" entityID=\"" + cmd.getApplicationId() + "\">\n" +
                "    <md:SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/oauth2/callback/saml\" index=\"1\"/>\n" +
                "    </md:SPSSODescriptor>\n" +
                "</md:EntityDescriptor>";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        return service;
    }

    private static RegexRegisteredService transformSentry(SentryConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<md:EntityDescriptor\n" +
                "    xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\" validUntil=\"2099-05-19T09:47:09Z\" cacheDuration=\"PT604800S\" entityID=\"" + url + "/saml/metadata/sentry/\">\n" +
                "    <md:SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"false\" protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <md:SingleLogoutService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect\" Location=\"" + url + "/saml/sls/sentry/\"/>\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" +
                "        <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/saml/acs/sentry/\" index=\"1\"/>\n" +
                "    </md:SPSSODescriptor>\n" +
                "</md:EntityDescriptor>";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        return service;
    }

    private static RegexRegisteredService transformJira(JiraConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<?xml version=\"1.0\"?>\n" +
                "<md:EntityDescriptor\n" +
                "    xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\"\n" +
                "                     validUntil=\"2099-06-20T11:57:29Z\"\n" +
                "                     cacheDuration=\"PT604800S\"\n" +
                "                     entityID=\"" + url + "\">\n" +
                "    <md:SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"true\"\n" +
                "                        protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" +
                "        <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/plugins/servlet/samlconsumer\"\n" +
                "                                     index=\"1\"/>\n" +
                "    </md:SPSSODescriptor>\n" +
                "</md:EntityDescriptor>\n";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        ((SamlRegisteredService) service).setSignAssertions(TriStateBoolean.TRUE);
        return service;
    }
    private static RegexRegisteredService transformConfluence(ConfluenceConfig cmd, RegexRegisteredService service) {
        if (null == service) {
            service = new SamlRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        String spMetadataContent = "<?xml version=\"1.0\"?>\n" +
                "<md:EntityDescriptor\n" +
                "    xmlns:md=\"urn:oasis:names:tc:SAML:2.0:metadata\"\n" +
                "                     validUntil=\"2099-06-20T11:57:29Z\"\n" +
                "                     cacheDuration=\"PT604800S\"\n" +
                "                     entityID=\"" + url + "\">\n" +
                "    <md:SPSSODescriptor AuthnRequestsSigned=\"false\" WantAssertionsSigned=\"true\"\n" +
                "                        protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>\n" +
                "        <md:AssertionConsumerService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" Location=\"" + url + "/plugins/servlet/samlconsumer\"\n" +
                "                                     index=\"1\"/>\n" +
                "    </md:SPSSODescriptor>\n" +
                "</md:EntityDescriptor>\n";
        ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(spMetadataContent.getBytes(
                StandardCharsets.UTF_8)));
        ((SamlRegisteredService) service).setSignAssertions(TriStateBoolean.TRUE);
        return service;
    }

    private static RegexRegisteredService transformAuthsaurAdmin(AuthsaurAdminConfig cmd,
                                                                 RegexRegisteredService service) {
        if (null == service) {
            service = new RegexRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + UriComponentsBuilder.fromUriString(url).path("/user").toUriString() + ".*";
        service.setServiceId(serviceId);
        return service;
    }

    private static RegexRegisteredService transformCodesign(CodesignConfig cmd,
                                                            RegexRegisteredService service) {
        if (null == service) {
            service = new RegexRegisteredService();
        }
        String url = cmd.getCallback();
        String serviceId = "^" + url + ".*";
        service.setServiceId(serviceId);
        return service;
    }

    private static RegexRegisteredService transformJumpServer(JumpServerConfig cmd,
                                                              RegexRegisteredService service) {
        if (null == service) {
            service = new RegexRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + UriComponentsBuilder.fromUriString(url).path("/core/auth/cas/login/").toUriString() + ".*";
        service.setServiceId(serviceId);
        return service;
    }

    private static RegexRegisteredService transformGrafana(GrafanaConfig cmd,
                                                           RegexRegisteredService service) {
        if (null == service) {
            service = new OidcRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + UriComponentsBuilder.fromUriString(url).path("/login/generic_oauth").toUriString() + ".*";
        service.setServiceId(serviceId);
        ((OidcRegisteredService) service).setClientId(cmd.getClientId());
        ((OidcRegisteredService) service).setClientSecret(cmd.getClientSecret());
        ((OidcRegisteredService) service).setBypassApprovalPrompt(true);
        return service;
    }

    private static RegexRegisteredService transformGerrit(GerritConfig cmd,
                                                          RegexRegisteredService service) {
        if (null == service) {
            service = new OAuthRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + UriComponentsBuilder.fromUriString(url).path("/oauth").toUriString() + ".*";
        service.setServiceId(serviceId);
        ((OAuthRegisteredService) service).setClientId(cmd.getClientId());
        ((OAuthRegisteredService) service).setClientSecret(cmd.getClientSecret());
        ((OAuthRegisteredService) service).setBypassApprovalPrompt(true);
        return service;
    }
    private static RegexRegisteredService transformSeafile(SeafileConfig cmd,
                                                          RegexRegisteredService service) {
        if (null == service) {
            service = new OAuthRegisteredService();
        }
        String url = cmd.getUrl();
        String serviceId = "^" + UriComponentsBuilder.fromUriString(url).path("/oauth/callback").toUriString() + ".*";
        service.setServiceId(serviceId);
        ((OAuthRegisteredService) service).setClientId(cmd.getClientId());
        ((OAuthRegisteredService) service).setClientSecret(cmd.getClientSecret());
        ((OAuthRegisteredService) service).setBypassApprovalPrompt(true);
        return service;
    }

    private RegexRegisteredService transform(ServiceCmd cmd, RegexRegisteredService service) {
        if ("CAS".equals(cmd.getType())) {
            if (null == service) {
                service = new RegexRegisteredService();
            }
        } else if ("OAuth2".equals(cmd.getType())) {
            if (null == service) {
                service = new OAuthRegisteredService();
            }
            ((OAuthRegisteredService) service).setClientId(cmd.getClientId());
            ((OAuthRegisteredService) service).setClientSecret(cmd.getClientSecret());
            ((OAuthRegisteredService) service).setBypassApprovalPrompt(!cmd.isShowApprovalPrompt());
        } else if ("Oidc".equals(cmd.getType())) {
            if (null == service) {
                service = new OidcRegisteredService();
            }
            ((OidcRegisteredService) service).setClientId(cmd.getClientId());
            ((OidcRegisteredService) service).setClientSecret(cmd.getClientSecret());
            ((OidcRegisteredService) service).setBypassApprovalPrompt(!cmd.isShowApprovalPrompt());
        } else if ("SAML".equals(cmd.getType())) {
            if (null == service) {
                service = new SamlRegisteredService();
            }
            String spMetadataContent = cmd.getSpMetadataContent();
            ((SamlRegisteredService) service).setMetadataLocation("base64://" + Base64.encodeBase64String(
                    spMetadataContent.getBytes(StandardCharsets.UTF_8)));
            ((SamlRegisteredService) service).setSignResponses(cmd.isSignResponses());
            ((SamlRegisteredService) service).setSignAssertions(TriStateBoolean.fromBoolean(cmd.isSignAssertions()));
        }
        service.setServiceId(cmd.getServiceId());
        return service;
    }

    private RegisteredService saveService(ServiceCmd cmd, RegexRegisteredService service) {
        service.setId(cmd.getId());
        service.setName(cmd.getName());
        service.setDescription(cmd.getDescription());
        service.setLogo(cmd.getLogo());
        service.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        // service properties
        Map<String, RegisteredServiceProperty> properties = service.getProperties();
        properties.put(ServicePropertyKey.TYPE.getAttr(), new DefaultRegisteredServiceProperty(cmd.getType()));
        properties.put(ServicePropertyKey.ALIAS.getAttr(), new DefaultRegisteredServiceProperty(cmd.getName()));
        properties.put(ServicePropertyKey.TAG.getAttr(), new DefaultRegisteredServiceProperty(cmd.getTag()));
        properties.put(ServicePropertyKey.HOME_PAGE.getAttr(), new DefaultRegisteredServiceProperty(cmd.getHomePage()));
        properties.put(ServicePropertyKey.SAML_REQUEST.getAttr(), new DefaultRegisteredServiceProperty(cmd.getSaml()));
        service.setProperties(properties);

        RegisteredService registeredService = serviceRegistry.save(service);
        if (null != cmd.getAppConfig() && Void.class != cmd.getAppConfig().getClass()) {
            JpaAppConfigEntity jpaAppConfigEntity = new JpaAppConfigEntity();
            jpaAppConfigEntity.setAppId(registeredService.getId());
            jpaAppConfigEntity.setAppType(cmd.getType());
            jpaAppConfigEntity.setConfigType(cmd.getAppConfig().getClass().getName());
            jpaAppConfigEntity.fromProperty(cmd.getAppConfig());
            jpaAppConfigRegistry.saveOrUpdate(jpaAppConfigEntity);
        }
        eventTrigger.trigger(AuthsaurEvent.SERVICE);
        return registeredService;
    }


}
