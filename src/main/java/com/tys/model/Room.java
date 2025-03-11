package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ROOM")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NUMBER")
    private Integer number;            // Odanın numarası

    @Column(name = "FULL")
    private Boolean full;              // Oda dolu mu? (true: dolu, false: boş)

    @Column(name = "CAPACITY")
    private Integer capacity;          // Odanın kapasitesi (kaç kişi kalabilir?)

    @Column(name = "SEA_VIEW")
    private Boolean seaView;            // Oda deniz manzaralı mı?

//    @ManyToOne
//    @JoinColumn(name="company", nullable=false)
//    private Company company;

}
