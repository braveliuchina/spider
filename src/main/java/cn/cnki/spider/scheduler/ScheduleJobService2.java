package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import org.quartz.SchedulerException;

import java.util.List;

public interface ScheduleJobService2 extends CommonService<ScheduleJobVo, ScheduleJob, Long> {

    List<ScheduleJobVo> list();

}
