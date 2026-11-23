package com.tys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "floor")
    private Integer floor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @JsonIgnore
    @ManyToMany(mappedBy = "rooms") // Burada 'rooms' ile eşleşmeli
    private List<Reservation> reservations;  // Relationship with Reservation

//    @OneToMany(mappedBy = "room")
//    private List<Guest> guestList;

//    @ManyToOne
//    @JoinColumn(name = "reservation_id")
//    private Reservation reservation;
}