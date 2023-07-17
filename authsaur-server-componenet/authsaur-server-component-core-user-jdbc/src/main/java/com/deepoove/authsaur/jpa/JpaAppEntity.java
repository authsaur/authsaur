package com.deepoove.authsaur.jpa;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.util.serialization.StringSerializer;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "access_strategy")
@Entity(name = JpaAppEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaAppEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaAppEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @Builder.Default
    private long id = -1;

    @Column(nullable = false, unique = true)
    private long appId;

    @Lob
    @Column(nullable = false, length = 8_000)
    private String accessStrategy;

    @Column(nullable = false)
    private String accessStrategyType;

    private static StringSerializer<RegisteredServiceAccessStrategy> SERIALIZER = new AppJsonSerializer(
            new MinimalPrettyPrinter());

    public <T> JpaAppEntity fromStrategy(final RegisteredServiceAccessStrategy t) {
        val jsonBody = SERIALIZER.toString(t);
        setAccessStrategy(jsonBody);
        return this;

    }

    public RegisteredServiceAccessStrategy readStrategy() {
        val service = SERIALIZER.from(this.accessStrategy);
        return service;
    }


}
