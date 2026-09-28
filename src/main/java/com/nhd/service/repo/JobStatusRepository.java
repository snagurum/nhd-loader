package com.nhd.service.repo;

import com.nhd.models.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {

    @Query("select js from JobStatus js where js.startTime > CURRENT_DATE")
    public List<JobStatus> getTodaysJobStatus();

    @Query("select js from JobStatus js where js.name = :name and js.startTime > CURRENT_DATE")
    public List<JobStatus>  getTodaysJobStatusByJobName(@Param("name") String name);
}
