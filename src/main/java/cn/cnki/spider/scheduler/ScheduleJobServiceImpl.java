package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.util.SpringUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ScheduleJobServiceImpl
        extends CommonServiceImpl<ScheduleJob, ScheduleJob, Long> implements ScheduleJobService {

    @PersistenceContext
    private EntityManager em;

    private final ScheduleJobRepository scheduleJobRepository;

    private final QuartzService quartzService;

    public ScheduleJobServiceImpl(ScheduleJobRepository scheduleJobRepository,
                                  QuartzService quartzService,
                                  CommonRepository<ScheduleJob, Long> commonRepository) {
        super(commonRepository);
        this.scheduleJobRepository = scheduleJobRepository;
        this.quartzService = quartzService;
    }

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
    public void startTemp(long id) throws SchedulerException {
        Result<ScheduleJob> jobResult = this.get(id);
        ScheduleJob job = (jobResult.getData());

        //获取对应的Bean
        Object object = SpringUtil.getBean(job.getBeanClass());
        try {

            Method[] methods = object.getClass().getDeclaredMethods();
            String methodName = job.getMethodName();
            for (int i=0;i< methods.length;i++) {
                String methodNameIter =methods[i].getName();
                if (!methodName.equals(methodNameIter)) {
                    continue;
                }
                Parameter[] parameters = methods[i].getParameters();

                if (parameters.length == 0) {
                    methods[i].invoke(object);
                    return;
                }
                Object[] actualParameters = new Object[parameters.length] ;
                String jobDataJsonString = job.getJobDataMap();
                JSONArray json = JSONArray.parseArray(jobDataJsonString);
                for (int j = 0; j < parameters.length; j++) {
                    actualParameters[j] = json.get(j);
                }
                methods[i].invoke(object, actualParameters);
            }

        } catch (Exception e) {
            log.warn("cron job error", e);
        }
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