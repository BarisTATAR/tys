package com.tys.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name="Room")
public class Room {

    @Id
    @Column(name = "roomNumber")
    private Long roomNumber;  // Odanın numarası

    @Column(name = "isFull")
    private Boolean isFull;        // Oda dolu mu? (true: dolu, false: boş)

    @Column(name = "capacity")
    private int capacity;          // Odanın kapasitesi (kaç kişi kalabilir?)

    @Column(name = "isSeaView")
    private Boolean isSeaView; // Oda deniz manzaralı mı?

//    @ManyToOne
//    @JoinColumn(name="company", nullable=false)
//    private Company company;

}
