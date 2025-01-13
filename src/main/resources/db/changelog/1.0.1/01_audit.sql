create table lt.job_status(
    id                integer primary key generated always as identity,
    type              varchar ,
    name              varchar,
    comments          text,
    status            varchar,
    start_time        timestamp,
    end_time          timestamp,
    duration          integer,
    success_count     integer,
    failure_count     integer
);
