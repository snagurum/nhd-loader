package com.nhd.service.repo;

import com.nhd.models.Holiday;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;


public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {

    boolean existsByHolidayDateAndActiveTrue(LocalDate holidayDate);

    Holiday findByHolidayDate(LocalDate holidayDate);
}
