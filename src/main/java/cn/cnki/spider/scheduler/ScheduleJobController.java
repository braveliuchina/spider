package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.Result;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/job")
public class ScheduleJobController {

    @Autowired
    private ScheduleJobService jobService;

    @GetMapping(value = "/list")
    public Result<List<ScheduleJob>> list() {

        return jobService.listAll();

    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ScheduleJob job) {
        ScheduleJob jobNew = job;
//        job.setJobName("任务02");
//        job.setCronExpression("0/2 * * * * ?");
        job.setBeanClass("crawlService");
        job.setMethodName("commonCrawl");
        job.setJobType("temp");
        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        }
        jobNew.setCtime(System.currentTimeMillis());
        jobNew.setUtime(System.currentTimeMillis());
        jobService.add(jobNew);
        return Result.of("新增定时任务成功");
    }

    @GetMapping("/start/{id}")
    public Result<String> start(@PathVariable("id") Long id) throws SchedulerException {
        jobService.start(id);
        return Result.of("启动定时任务成功");
    }

    @GetMapping("/startTemp/{id}")
    public Result<String> startTemp(@PathVariable("id") Long id) throws SchedulerException {
        jobService.startTemp(id);
        return Result.of("启动任务成功");
    }

    @GetMapping("/state/{id}")
    public Result<String> state(@PathVariable("id") Long id) throws SchedulerException {
        return Result.of(jobService.status(id));
    }

    @GetMapping("/startWithParam/{id}")
    public Result<String> start(@PathVariable("id") Long id,
                        @RequestBody Map<String, Object> requestMap) throws SchedulerException {

        jobService.start(id);
        return Result.of("启动定时任务成功");
    }

    @GetMapping("/pause/{id}")
    public Result<String> pause(@PathVariable("id") Long id) throws SchedulerException {
        jobService.pause(id);
        return Result.of("暂停定时任务成功");
    }

    @GetMapping("/delete/{id}")
    public Result<String> delete(@PathVariable("id") Long id) throws SchedulerException {
        pause(id);
        jobService.delete(id);
        return Result.of("删除定时任务成功");
    }

    @GetMapping("/enableAll")
    public Result<String> startAllJob() {
        jobService.startAllJob();
        return Result.of("启动所有定时任务成功");
    }

    @GetMapping("/disableAll")
    public Result<String> pauseAllJob() {
        jobService.pauseAllJob();
        return Result.of("暂停所有定时任务成功");
    }
}