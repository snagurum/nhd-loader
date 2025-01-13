package com.nhd.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;

@Data
@Table(name = "job_status", schema = "lt")
public class JobStatus {

    @Id
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
