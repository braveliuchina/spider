package cn.cnki.spider.scheduler;

import org.quartz.SchedulerException;

public interface QuartzService {

    public void addJob(ScheduleJobVo job);

    public void timingTask();

    public void operateJob(JobOperateEnum jobOperateEnum, ScheduleJobVo job) throws SchedulerException;

    public void startAllJob() throws SchedulerException;

    public void pauseAllJob() throws SchedulerException;

    public String fetchStatus(ScheduleJobVo job) throws SchedulerException;
}
