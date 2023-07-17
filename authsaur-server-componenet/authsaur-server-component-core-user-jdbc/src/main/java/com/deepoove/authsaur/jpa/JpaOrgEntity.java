package com.deepoove.authsaur.jpa;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "org")
@Entity(name = JpaOrgEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaOrgEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaOrgEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    private String id;

    @Column(nullable = false)
    private String source;
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String parentId;

    @Column(nullable = true)
    private String path;

    @Column(nullable = true)
    private long orderSort;

    @Column(nullable = true, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private Date created;

    @Column(nullable = true, columnDefinition = "datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP")
    private Date updated;


}
