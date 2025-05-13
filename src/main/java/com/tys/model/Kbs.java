package com.tys.model;

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

    @Column(name = "kln_tc")
    private String klnTc;   //Company T.C No

    @Column(name = "company_code")
    private String companyCode;     //Company Code

    @Column(name = "kln_password")
    private String klnPassword;     //kriptolanacak

    @OneToOne(mappedBy = "company")
    private Company company;
}
