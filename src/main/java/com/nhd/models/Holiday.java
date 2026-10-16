package com.nhd.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "holiday",schema = "lt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday {

    @Id
    @Column(name = "holiday_date")
    private LocalDate holidayDate;

    @Column(name = "week_day", nullable = false, length = 9)
    private String weekDay;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
}
