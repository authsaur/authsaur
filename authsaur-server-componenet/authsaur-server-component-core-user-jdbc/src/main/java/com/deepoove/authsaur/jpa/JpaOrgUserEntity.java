package com.deepoove.authsaur.jpa;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "org_user", uniqueConstraints = {@UniqueConstraint(columnNames = {"orgId", "principal"})})
@Entity(name = JpaOrgUserEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaOrgUserEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaOrgUserEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @Builder.Default
    private long id = -1;

    @Column(nullable = false)
    private String orgId;

    @Column(nullable = false)
    private String orgPath;

    @Column(nullable = true)
    private String principal;

}
