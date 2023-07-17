package com.deepoove.authsaur.services;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


@Slf4j
@ToString(callSuper = true)
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RenamedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private Map<String, String> renamedAttributes = new TreeMap<>();

    @JsonCreator
    public RenamedAttributeReleasePolicy(
            @JsonProperty("renamedAttributes") final Map<String, String> attributes) {
        this.renamedAttributes = attributes;
    }

    public Map<String, String> getRenamedAttributes() {
        return new TreeMap<>(this.renamedAttributes);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(
            final RegisteredServiceAttributeReleasePolicyContext context,
            final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);
        val attributesToRelease = new HashMap<String, List<Object>>();
        resolvedAttributes.forEach((attr, value) -> {
            String key = attr;
            if (getRenamedAttributes().containsKey(attr)) {
                String str = getRenamedAttributes().get(attr);
                if (StringUtils.isNotBlank(str)) {
                    key = str;
                }
            }
            attributesToRelease.put(key, value);
        });

        return attributesToRelease;
    }

}
