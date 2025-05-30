package com.tys.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    private BigDecimal ratingAverage;           // Puan (1-5 arası)

    @Column(name = "cleanliness")
    private Integer cleanliness;   // Temizlik değerlendirmesi

    @Column(name = "service")
    private Integer service;       // Hizmet değerlendirmesi

    @Column(name = "comfort")
    private Integer comfort;       // Konfor değerlendirmesi

    @Column(name = "location")
    private Integer location;      // Konum değerlendirmesi

    @Column(name = "food")
    private Integer food;      // Yiyecek  değerlendirmesi

}
