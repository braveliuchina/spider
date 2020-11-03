package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleJobRepository extends CommonRepository<ScheduleJob, Long> {

    List<ScheduleJob> findByJobStatus(String jobStatus);

    List<ScheduleJob> findByJobName(String name);

}