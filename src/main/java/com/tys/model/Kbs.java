package com.tys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "kbs")
public class Kbs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @JsonIgnore

    @Column(name = "identityNumber")
    private String identityNumber;   //Company T.C No

    @Column(name = "company_code")
    private String companyCode;     //Company Code

    @JsonIgnore

    @Column(name = "password")
    private String password;     // AES-GCM ile şifreli (SecretCipher)

    @OneToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
