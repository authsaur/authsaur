package com.deepoove.authsaur.jpa;

import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "user", uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "source"})})
@Entity(name = JpaUserEntity.ENTITY_NAME)
@Getter
@Setter
@ToString
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class JpaUserEntity implements Serializable {
    /**
     * Th JPA entity name.
     */
    public static final String ENTITY_NAME = "JpaUserEntity";

    private static final long serialVersionUID = 6534421912995436609L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @Builder.Default
    private long id = -1;

    @Column(nullable = false, unique = true)
    private String principal;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String source;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String email;

    @Column(nullable = true)
    private String mobile;

    @Lob
    @Column(nullable = true, length = 8_000)
    private String avatar;

    @Column(nullable = true)
    private Boolean state = false;

    @Column(nullable = true, columnDefinition = "datetime DEFAULT CURRENT_TIMESTAMP")
    private Date created;

    @Column(nullable = true, columnDefinition = "datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP")
    private Date updated;


}
