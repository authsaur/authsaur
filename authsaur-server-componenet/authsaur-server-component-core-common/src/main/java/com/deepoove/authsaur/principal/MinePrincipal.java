package com.deepoove.authsaur.principal;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;
import org.apereo.cas.authentication.principal.Principal;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ToString
@Getter
@NoArgsConstructor
public class MinePrincipal implements Principal {

    private static final long serialVersionUID = -1255260750151385796L;

    /**
     * The unique identifier for the principal.
     */
    private String id;
    private String userId;
    private String name;
    private String mail;
    private String phone;
    private String avatar;
    private List<String> dept;
    private String authId;
    private String authType;
    private MinePrincipal relatedPrincipal = null;


    /**
     * Principal attributes.
     **/
    private Map<String, List<Object>> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Instantiates a new simple principal.
     *
     * @param id         the id
     * @param attributes the attributes
     */
    public MinePrincipal(final @NonNull String id,
                         final Map<String, List<Object>> attributes) {
        this.id = id;
        this.attributes.putAll(attributes);

        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.USER_ID.getAttr()))) {
            this.userId = String.valueOf(attributes.get(PrincipalAttr.USER_ID.getAttr()).get(0));
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.NAME.getAttr()))) {
            this.name = String.valueOf(attributes.get(PrincipalAttr.NAME.getAttr()).get(0));
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.AVATAR.getAttr()))) {
            this.avatar = String.valueOf(attributes.get(PrincipalAttr.AVATAR.getAttr()).get(0));
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.DEPT.getAttr()))) {
            this.dept = attributes.get(PrincipalAttr.DEPT.getAttr()).stream().map(String::valueOf).collect(
                    Collectors.toList());
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.AUTH_ID.getAttr()))) {
            this.authId = String.valueOf(attributes.get(PrincipalAttr.AUTH_ID.getAttr()).get(0));
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.AUTH_TYPE.getAttr()))) {
            this.authType = String.valueOf(attributes.get(PrincipalAttr.AUTH_TYPE.getAttr()).get(0));
        }
        if (null != attributes && CollectionUtils.isNotEmpty(attributes.get(PrincipalAttr.RELATED.getAttr()))) {
            this.relatedPrincipal = new MinePrincipal(null, (Map<String, List<Object>>) attributes.get(PrincipalAttr.RELATED.getAttr()).get(0));
        }

    }

    public MinePrincipal(final @NonNull Principal principal) {
        this(principal.getId(), principal.getAttributes());
    }


}

