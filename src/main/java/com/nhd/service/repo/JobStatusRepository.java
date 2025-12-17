package com.nhd.service.repo;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nhd.models.JobStatus;

@Repository
public interface JobStatusRepository extends CrudRepository<JobStatus, Long> {

    @Query("select js.* from lt.Job_Status js where js.start_time > CURRENT_DATE")
    public List<JobStatus> getTodaysJobStatus();

    @Query("select js.* from lt.Job_Status js where js.name = :name and js.start_time > CURRENT_DATE")
    public List<JobStatus>  getTodaysJobStatusByJobName(@Param("name") String name);

    @Query("select count(*) from lt.Job_Status js where js.name = :name and js.start_time > CURRENT_DATE and js.end_time is not null")
    public int isJobCompletedForToday(@Param("name") String name);


}
