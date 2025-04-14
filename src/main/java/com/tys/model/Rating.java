package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(schema = "tys", name = "rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rating_average")
    private double ratingAverage;           // Puan (1-5 arası)

    @Column(name = "cleanliness")
    private int cleanliness;   // Temizlik değerlendirmesi

    @Column(name = "service")
    private int service;       // Hizmet değerlendirmesi

    @Column(name = "comfort")
    private int comfort;       // Konfor değerlendirmesi

    @Column(name = "location")
    private int location;      // Konum değerlendirmesi

    @Column(name = "food")
    private int food;      // Yiyecek  değerlendirmesi

}
