package com.nhd.models;

import lombok.Data;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "job_status", schema = "lt")
public class JobStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String name;

    private String comments;

    private String status;

    private Timestamp startTime;

    private Timestamp endTime;

    private Long duration;

    private int successCount;

    private int failureCount;
}
