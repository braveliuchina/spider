package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.service.CommonService;
import org.quartz.SchedulerException;

import java.util.List;

public interface ScheduleJobService extends CommonService<ScheduleJob, ScheduleJob, Long> {

    List<ScheduleJob> list();

    List<ScheduleJob> listByJobStatus(String status);

    void add(ScheduleJob job);

    void start(long id) throws SchedulerException;

    void startTemp(long id) throws SchedulerException;

    void pause(long id) throws SchedulerException;

    void delete(long id) throws SchedulerException;

    String status(long id) throws SchedulerException;

    void startAllJob();

    void pauseAllJob();

}
