package com.deepoove.authsaur.response;

import lombok.Data;

import java.util.Map;

@Data
public class ServiceAttributeDTO {
    private String principalAttr;
    Map<String, String> renamedAttributes;
}
