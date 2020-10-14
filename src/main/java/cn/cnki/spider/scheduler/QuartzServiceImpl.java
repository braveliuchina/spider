package cn.cnki.spider.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
public class QuartzServiceImpl implements QuartzService {

    /**
     * 调度器
     */
    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleJobService jobService;

    @Override
    public void timingTask() {

        //查询数据库是否存在需要定时的任务
        List<ScheduleJob> scheduleJobs = jobService.listAll().getData();
        if (scheduleJobs != null) {
            scheduleJobs.forEach(this::addJob);
        }
    }

    @Override
    public void addJob(ScheduleJob job) {
        try {
            //创建触发器
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .startNow()
                    .build();

            //创建任务
            JobDetail jobDetail = JobBuilder.newJob(QuartzFactory.class)
                    .withIdentity(job.getJobName())
                    .build();
            //传入调度的数据，在QuartzFactory中需要使用
            jobDetail.getJobDataMap().put("scheduleJob", job);

            //调度作业
            scheduler.scheduleJob(jobDetail, trigger);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void operateJob(JobOperateEnum jobOperateEnum, ScheduleJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName());
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            log.warn("there is no job names this jobKey, {}", jobKey);
            return;
        }
        switch (jobOperateEnum) {
            case START:
                scheduler.resumeJob(jobKey);
                break;
            case PAUSE:
                scheduler.pauseJob(jobKey);
                break;
            case DELETE:
                scheduler.deleteJob(jobKey);
                break;
        }
    }

    @Override
    public void startAllJob() throws SchedulerException {
        scheduler.start();
    }

    @Override
    public void pauseAllJob() throws SchedulerException {
        scheduler.standby();
    }

    @Override
    public String fetchStatus(ScheduleJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName());
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup());
        return String.valueOf(scheduler.getTriggerState(triggerKey));
    }
}