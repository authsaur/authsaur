package com.deepoove.authsaur.jpa;

import com.deepoove.authsaur.util.JSONHelper;
import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.JsonUtils;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "AuthenticationHandlers")
@Entity(name = JpaAuthenticationEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaAuthenticationEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaAuthEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
//    @GeneratedValue(generator = "uuid")
//    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Builder.Default
    private String id = "-1";

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String state;

    @Column(nullable = true)
    private String relatedAuthnId;

    @Column(nullable = true)
    private Boolean relatedWithMail;
    @Column(nullable = true)
    private Boolean relatedWithPhone;

    @Column(nullable = false)
    private String bodyType;

    @Lob
    @Column(nullable = false, length = 8_000)
    private String body;

    public <T> JpaAuthenticationEntity fromProperty(final T t) {
        setBody(JSONHelper.toStr(t));
        return this;
    }

    public <T> T readProperty() {
        try {
            return (T) JSONHelper.fromStr(body, Class.forName(bodyType));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readProperty(Class<T> clazz) {
        return (T) JSONHelper.fromStr(body, clazz);
    }


}
