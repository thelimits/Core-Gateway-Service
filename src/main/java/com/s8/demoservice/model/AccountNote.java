package com.s8.demoservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name="AccountNote")
public class AccountNote {
    @Id
    private String id;

    private String note;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonIgnore
    private Account account;
}
