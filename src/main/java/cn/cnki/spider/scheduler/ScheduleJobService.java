package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import org.quartz.SchedulerException;

import java.util.List;

public interface ScheduleJobService extends CommonService<ScheduleJobVo, ScheduleJob, Long> {

    List<ScheduleJobVo> list();

    List<ScheduleJob> listByJobStatus(String status);

    void add(ScheduleJobVo job) throws Exception;

    Result<ScheduleJobVo> edit(ScheduleJobVo job) throws Exception;

    void start(long id) throws SchedulerException;

    void startTemp(long id) throws Exception;

    void pause(long id) throws SchedulerException;

    void delete(long id) throws SchedulerException;

    String status(long id) throws SchedulerException;

    int fetchStatus(long id);

    void startAllJob();

    void pauseAllJob();

}
