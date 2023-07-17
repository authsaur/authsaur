package com.deepoove.authsaur.jpa;

import com.deepoove.authsaur.util.JSONHelper;
import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "service_config")
@Entity(name = JpaAppConfigEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaAppConfigEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaAppConfigEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @Builder.Default
    private long id = -1;

    @Column(nullable = false, unique = true)
    private long appId;

    @Column(nullable = false)
    private String appType;

    @Lob
    @Column(nullable = false, length = 8_000)
    private String config;

    @Column(nullable = false)
    private String configType;

    public <T> JpaAppConfigEntity fromProperty(final T t) {
        setConfig(JSONHelper.toStr(t));
        return this;
    }

    public <T> T readProperty() {
        try {
            return (T) JSONHelper.fromStr(config, Class.forName(configType));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T readProperty(Class<T> clazz) {
        return (T) JSONHelper.fromStr(config, clazz);
    }

}
