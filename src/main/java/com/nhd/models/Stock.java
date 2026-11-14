package com.nhd.models;

import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.sql.Date;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "stocks", schema = "lt")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;

    private String name;

    private String series;

    private Date dateOfListing;

    private boolean historyLoaded;

    /** Date on which BSP history was last requested for this ticker. */
    private LocalDate bspLoadedDate;

    private boolean active;

}

