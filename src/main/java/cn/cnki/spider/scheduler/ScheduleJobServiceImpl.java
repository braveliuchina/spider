package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.HistoryDO;
import cn.cnki.spider.common.pojo.PageInfo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.sys.sysuser.pojo.SysUser;
import cn.cnki.spider.sys.sysuser.vo.SysUserVo;
import cn.cnki.spider.util.CommonForkJoinPool;
import cn.cnki.spider.util.SpringUtil;
import cn.cnki.spider.util.SqlUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ScheduleJobServiceImpl
        extends CommonServiceImpl<ScheduleJobVo, ScheduleJob, Long> implements ScheduleJobService {

    @PersistenceContext
    private EntityManager em;

    private final ScheduleJobRepository scheduleJobRepository;

    private final MongoTemplate mongoTemplate;

    private final QuartzService quartzService;

    private final CommonForkJoinPool forkJoinPool = new CommonForkJoinPool(16, "crawlPool");

    public ScheduleJobServiceImpl(ScheduleJobRepository scheduleJobRepository,
                                  QuartzService quartzService,
                                  CommonRepository<ScheduleJob, Long> commonRepository,
                                  MongoTemplate mongoTemplate) {
        super(commonRepository);
        this.scheduleJobRepository = scheduleJobRepository;
        this.quartzService = quartzService;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Result<PageInfo<ScheduleJobVo>> page(ScheduleJobVo entityVo) {
        //根据实体、Vo直接拼接全部SQL
        StringBuilder sql = SqlUtil.joinSqlByEntityAndVo(ScheduleJob.class,entityVo);

        //设置SQL、映射实体，以及设置值，返回一个Query对象
        Query query = em.createNativeQuery(sql.toString(), ScheduleJob.class);

        //分页设置，page从0开始
        PageRequest pageRequest = PageRequest.of(entityVo.getPage() - 1, entityVo.getRows());

        //获取最终分页结果
        Result<PageInfo<ScheduleJobVo>> result = Result.of(PageInfo.of(PageInfo.getJPAPage(query,pageRequest,em), ScheduleJobVo.class));

        return result;
    }

    @Override
    public List<ScheduleJobVo> list() {
        ScheduleJobVo job = new ScheduleJobVo();
        Result<List<ScheduleJobVo>> listResult = this.list(job);
        return listResult.getData();
    }

    @Override
    public List<ScheduleJob> listByJobStatus(String status) {
        return scheduleJobRepository.findByJobStatus(status);
    }

    @Override
    public void add(ScheduleJobVo job) throws Exception {

        List<ScheduleJob> jobs = scheduleJobRepository.findByJobName(job.getJobName());
        if (null != jobs && !jobs.isEmpty()) {
            throw new Exception("duplicated job name, enter new one please");
        }
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
    public Result<ScheduleJobVo> edit(ScheduleJobVo job) throws Exception {
        List<ScheduleJob> jobs = scheduleJobRepository.findByJobName(job.getJobName());
        if (null != jobs && !jobs.isEmpty()) {
            Long id = job.getId();
            if (!jobs.get(0).getId().equals(id)) {
                throw new Exception("duplicated job name, enter new one please");
            }
        }
        return this.save(job);
    }

    @Override
    public void start(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJobVo> jobResult = this.get(id);
        ScheduleJobVo job = (jobResult.getData());
        job.setEnable(1);
        this.save(job);

        //执行job
        quartzService.operateJob(JobOperateEnum.START, job);
    }

    @Override
    public void startTemp(long id) throws Exception {
        Optional<ScheduleJob> jobOptional = scheduleJobRepository.findById(id);
        if (!jobOptional.isPresent()) {
            throw new Exception("there is no job with this id");
        }
        ScheduleJob job = jobOptional.get();
        String jobType = job.getJobType();
        if ("scheduler".equals(jobType)) {
            // 定时任务调用启用接口
            start(id);
            return;
        }
        job.setUtime(System.currentTimeMillis());
        job.setJobStatus("1");
        scheduleJobRepository.saveAndFlush(job);
        // 保存执行记录
        HistoryDO historyDO = new HistoryDO();
        historyDO.setJobId(job.getId());
        historyDO.setStatus(1);
        Long now = System.currentTimeMillis();
        historyDO.setCtime(now);
        historyDO.setUtime(now);
        HistoryDO historyDO1 = mongoTemplate.save(historyDO);
        String hisId = historyDO1.getId();
        //获取对应的Bean
        Object object = SpringUtil.getBean(job.getBeanClass());
        try {

            Method[] methods = object.getClass().getDeclaredMethods();
            String methodName = job.getMethodName();
            for (int i=0;i< methods.length;i++) {
                String methodNameIter = methods[i].getName();
                if (!methodName.equals(methodNameIter)) {
                    continue;
                }
                Parameter[] parameters = methods[i].getParameters();

                if (parameters.length == 0) {
                    methods[i].invoke(object);
                    return;
                }
//                if ("templateCrawl".equals(methodName)) {
//
//                    String jobDataJsonString = job.getJobDataMap();
//                    JSONArray json = JSONArray.parseArray(jobDataJsonString);
//                    if (json.size() < 2 ) {
//                        log.error("爬取失败");
//                        return;
//                    }
//
//                    int finalI = i;
//                    forkJoinPool.submit(() -> methods[finalI]
//                            .invoke(object, new Long[] {Long.parseLong(String.valueOf(json.get(1)))}));
//                    return;
//                }
                Object[] actualParameters = new Object[parameters.length] ;
                String jobDataJsonString = job.getJobDataMap();
                JSONArray json = JSONArray.parseArray(jobDataJsonString);

                actualParameters[0] = job.getId();
                actualParameters[1] = jobType;
                actualParameters[2] = hisId;
                int start = 3;
                for (int j = 0; j < parameters.length - start; j++) {
                    actualParameters[start + j] = json.get(j);
                }
                int finalI = i;
                forkJoinPool.submit(() -> methods[finalI].invoke(object, actualParameters));

            }

        } catch (Exception e) {
            log.warn("cron job error", e);
        }
    }

    @Override
    public void pause(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJobVo> jobResult = this.get(id);
        ScheduleJobVo job = (jobResult.getData());

        if ("temp".equals(job.getJobType())) {
            return;
        }
        job.setEnable(0);
        this.save(job);

        //执行job
        quartzService.operateJob(JobOperateEnum.PAUSE, job);
    }

    @Override
    public void delete(long id) throws SchedulerException {
        //此处省去数据验证
        Result<ScheduleJobVo> jobResult = this.get(id);
        ScheduleJobVo job = (jobResult.getData());
        this.delete(id);

        //执行job
        quartzService.operateJob(JobOperateEnum.DELETE, job);
    }

    @Override
    public String status(long id) throws SchedulerException {
        Result<ScheduleJobVo> jobResult = this.get(id);
        ScheduleJobVo job = (jobResult.getData());
        return quartzService.fetchStatus(job);
    }

    @Override
    public int fetchStatus(long id) {
        Result<ScheduleJobVo> jobResult = this.get(id);
        ScheduleJobVo job = (jobResult.getData());
        String status = job.getJobStatus();
        return StringUtils.isBlank(status)? 0 : Integer.parseInt(status);
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