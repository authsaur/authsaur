package com.deepoove.authsaur.rest.controller;

import com.deepoove.authsaur.services.ServicePropertyKey;
import org.apache.commons.codec.binary.Base64;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.Deflater;

@RestController
@RequestMapping(value = "/api/saml", produces = {"application/json;charset=UTF-8"})
public class SamlApi {

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casConfigurationProperties;

    @GetMapping("/samluri")
    public void samluri(long id, HttpServletResponse response) throws IOException {
        RegisteredService service = servicesManager.findServiceBy(id);
        if (service instanceof SamlRegisteredService) {
            Map<String, RegisteredServiceProperty> properties = service.getProperties();
            RegisteredServiceProperty registeredServiceProperty = properties.get(
                    ServicePropertyKey.SAML_REQUEST.getAttr());
            if (null != registeredServiceProperty) {
                String request = registeredServiceProperty.getValue();
                if (null != request) {
                    String param = URLEncoder.encode(
                            Base64.encodeBase64String(deflate(request.getBytes(StandardCharsets.UTF_8))),
                            StandardCharsets.UTF_8);
                    response.sendRedirect(casConfigurationProperties.getAuthn().getSamlIdp().getCore().getEntityId() +
                            "/profile" +
                            "/SAML2/Redirect/SSO?SAMLRequest=" + param);
                }
            }
        }
    }

    public static byte[] deflate(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                // 压缩并将压缩后的内容输出到字节输出流bos中
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }


}
