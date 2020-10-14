package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageInfo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.sys.sysmenu.repository.SysMenuRepository;
import cn.cnki.spider.sys.sysmenu.vo.SysMenuVo;
import cn.cnki.spider.sys.syssetting.repository.SysSettingRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ScheduleJobServiceImpl
        extends CommonServiceImpl<ScheduleJob, ScheduleJob, Long> implements ScheduleJobService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ScheduleJobRepository scheduleJobRepository;

    @Autowired
    private QuartzService quartzService;

    @Override
    public List<ScheduleJob> list() {
        ScheduleJob job = new ScheduleJob();
        job.setJobStatus("1");
        Result<List<ScheduleJob>> listResult = this.list(job);
        return listResult.getData();
    }

    @Override
    public List<ScheduleJob> listByJobStatus(String status) {
        return scheduleJobRepository.findByJobStatus(status);
    }

    @Override
    public void add(ScheduleJob job) {

        //此处省去数据验证
        this.save(job);

        //加入job
        try {
            quartzService.addJob(job);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJob> jobResult = this.get(id);
        ScheduleJob job = (jobResult.getData());
        job.setJobStatus("1");
        this.save(job);

        //执行job
        quartzService.operateJob(JobOperateEnum.START, job);
    }

    @Override
    public void pause(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJob> jobResult = this.get(id);
        ScheduleJob job = (jobResult.getData());
        job.setJobStatus("2");
        this.save(job);

        //执行job
        quartzService.operateJob(JobOperateEnum.PAUSE, job);
    }

    @Override
    public void delete(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJob> jobResult = this.get(id);
        ScheduleJob job = (jobResult.getData());
        this.delete(id);

        //执行job
        quartzService.operateJob(JobOperateEnum.DELETE, job);
    }

    @Override
    public String status(long id) throws SchedulerException {
        Result<ScheduleJob> jobResult = this.get(id);
        ScheduleJob job = (jobResult.getData());
        return quartzService.fetchStatus(job);
    }

    @Override
    public void startAllJob() {
        //执行job
        try {
            quartzService.startAllJob();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pauseAllJob() {
        //执行job
        try {
            quartzService.pauseAllJob();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}