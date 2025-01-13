package com.nhd.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhd.models.JobStatus;
import com.nhd.service.repo.JobStatusRepository;
import com.nhd.util.JobName;


@Service
public class AuditServiceImpl implements AuditService{

    @Autowired
    JobStatusRepository jobStatusRepo;

    public JobStatus startJob(JobName jobName ) {
        return startJobWithComment(jobName,null);
    }

    public JobStatus startJobWithComment(JobName jobName,String comment){
        JobStatus job = new JobStatus();
        job.setType(jobName.getJobType());
        job.setName(jobName.toString());
        job.setComments(comment);
        job.setStartTime(new Timestamp(System.currentTimeMillis()));
        job.setStatus("Started");
        return jobStatusRepo.save(job);
    }

    public void closeJob(JobStatus job){
        job.setEndTime(new Timestamp(System.currentTimeMillis()));
        job.setDuration( (job.getEndTime().getTime()-job.getStartTime().getTime())/1000);
        jobStatusRepo.save(job);
    }

    public void endJob(JobStatus job) {
        job.setStatus("Completed");
        this.closeJob(job);
    }

    public void failJob(JobStatus job) {
        job.setStatus("Failure");
        this.closeJob(job);
    }

    public void endJobWithSuccessFailureCount(JobStatus job,int success, int failure) {
        job.setSuccessCount(success);
        job.setFailureCount(failure);
        this.endJob(job);
    }

    public void failJobWithSuccessFailureCount(JobStatus job,int success, int failure) {
        job.setSuccessCount(success);
        job.setFailureCount(failure);
        this.failJob(job);
    }

    public List<JobStatus> getTodaysJobStatus(){
        return jobStatusRepo.getTodaysJobStatus();
    }

    public List<JobStatus> getTodaysJobStatusByJobName(String jobName) {
        return jobStatusRepo.getTodaysJobStatusByJobName(jobName);
    }

}
