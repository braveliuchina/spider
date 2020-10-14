package cn.cnki.spider.scheduler;

import org.quartz.SchedulerException;

public interface QuartzService {

    public void addJob(ScheduleJob job);

    public void timingTask();

    public void operateJob(JobOperateEnum jobOperateEnum, ScheduleJob job) throws SchedulerException;

    public void startAllJob() throws SchedulerException;

    public void pauseAllJob() throws SchedulerException;

    public String fetchStatus(ScheduleJob job) throws SchedulerException;
}
