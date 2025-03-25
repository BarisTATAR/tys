package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "number")
    private Integer number;            // Odanın numarası

    @Column(name = "loaded")
    private Boolean loaded;              // Oda dolu mu? (true: dolu, false: boş)

    @Column(name = "capacity")
    private Integer capacity;          // Odanın kapasitesi (kaç kişi kalabilir?)

    @Column(name = "sea_view")
    private Boolean seaView;            // Oda deniz manzaralı mı?

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Guest> guestList;

}
