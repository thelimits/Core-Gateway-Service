package com.s8.demoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.s8.demoservice.model.enums.CustomerStatusType;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="CustomerKYC")
public class CustomerKYC extends DateAuditEntity {
    @Id
    private String id;

    private String firstName;

    private String lastName;

    private String address;

    @Column(unique = true)
    private String nik;

    private Date dob;

    private String motherMaidenName;

    @Column(unique = true)
    private String email;

    private String pin;

    @Column(unique = true)
    private String phoneNumber;

    @ApiModelProperty(hidden = true)
    @Enumerated(EnumType.STRING)
    private CustomerStatusType status;

    @OneToMany(
            mappedBy = "customer",
            fetch = FetchType.EAGER,
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private Set<Account> accounts;

    @PreUpdate
    protected void prePersist() {
        if (this.getActivatedAt() == null && this.status == CustomerStatusType.ACTIVE) this.setActivatedAt(Instant.now());
    }

}
