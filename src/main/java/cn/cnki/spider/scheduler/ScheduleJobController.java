package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.pojo.HtmlDO;
import cn.cnki.spider.common.pojo.PageInfo;
import cn.cnki.spider.common.pojo.Result;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class ScheduleJobController {

    private final ScheduleJobService jobService;

    private final MongoTemplate mongoTemplate;

    @GetMapping(value = "/list")
    public Result<PageInfo<ScheduleJobVo>> list(HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isBlank(page) || StringUtils.isBlank(rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        ScheduleJobVo vo = new ScheduleJobVo();
        vo.setPage(Integer.parseInt(page));
        vo.setRows(Integer.parseInt(rows));
        return jobService.page(vo);

    }

    @GetMapping(value = "/query/{id}")
    public Result<Object> list(@PathVariable("id") Long id) {

        Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is("temp");
            query.addCriteria(criteria1.andOperator(criteria2));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
            return Result.of(commonHtmlDO.getContent());
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        HtmlDO htmlDO = mongoTemplate.findOne(query, HtmlDO.class);
        return Result.of(htmlDO.getHtml());
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody ScheduleJobVo jobVO) {
        String name = jobVO.getJobName();
        String jobDesc = jobVO.getJobDesc();
        String url = jobVO.getUrl();
        List<String> xpathList = jobVO.getXpathList();
        if (StringUtils.isBlank(url) || null == xpathList || xpathList.isEmpty()) {
            return new Result(null, false, "param invalid");
        }
        ScheduleJobVo job = new ScheduleJobVo();
//        job.setJobName("任务02");
//        job.setCronExpression("0/2 * * * * ?");
        job.setJobName(name);
        job.setBeanClass("crawlService");
        job.setMethodName("commonCrawlV2");
        job.setJobType("temp");
        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(url);
        JSONArray jsonArray2 = new JSONArray();
        for (int i = 0; i < xpathList.size(); i++) {
            jsonArray2.add(xpathList.get(i));
        }
        jsonArray.add(jsonArray2);
        job.setJobDataMap(JSON.toJSONString(jsonArray));
        job.setJobDesc(jobDesc);
        job.setCtime(System.currentTimeMillis());
        job.setUtime(System.currentTimeMillis());
        try {
            jobService.add(job);
        } catch (Exception e) {
            return new Result("", false, e.getMessage());
        }
        return Result.of("job add successfully");
    }

    @PostMapping("/add/v2")
    public Result<String> addV2(@RequestBody ScheduleJobVo jobVO) {
        String name = jobVO.getJobName();
        String jobDesc = jobVO.getJobDesc();
        String url = jobVO.getUrl();
        List<String> xpathList = jobVO.getXpathList();
        if (StringUtils.isBlank(url)) {
            return new Result(null, false, "param invalid");
        }

        // 爬取html源码
        if (null != xpathList && !xpathList.isEmpty()) {
            return add(jobVO);
        }
        ScheduleJobVo job = new ScheduleJobVo();
        job.setJobName(name);
        job.setBeanClass("crawlService");
        job.setMethodName("seleniumCrawlHtmlAndSave");
        job.setJobType("temp");
        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(url);
        job.setJobDataMap(JSON.toJSONString(jsonArray));
        job.setJobDesc(jobDesc);
        job.setCtime(System.currentTimeMillis());
        job.setUtime(System.currentTimeMillis());
        try {
            jobService.add(job);
        } catch (Exception e) {
            return new Result("", false, e.getMessage());
        }
        return Result.of("job add successfully");
    }

    @PostMapping("/edit")
    public Result<ScheduleJobVo> edit(@RequestBody ScheduleJobVo jobVO) {
        String name = jobVO.getJobName();
        String jobDesc = jobVO.getJobDesc();
        Long id = jobVO.getId();

        if (null == id) {
            return new Result<>(new ScheduleJobVo(), false, "id should be passed when update");
        }
        String url = jobVO.getUrl();
        List<String> xpathList = jobVO.getXpathList();
        if (StringUtils.isBlank(url) || null == xpathList || xpathList.isEmpty()) {
            return new Result(null, false, "param invalid");
        }
        ScheduleJobVo job = new ScheduleJobVo();
//        job.setJobName("任务02");
//        job.setCronExpression("0/2 * * * * ?");
        job.setJobName(name);
        job.setBeanClass("crawlService");
        job.setMethodName("commonCrawlV2");
        job.setJobType("temp");
        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(url);
        JSONArray jsonArray2 = new JSONArray();
        for (int i = 0; i < xpathList.size(); i++) {
            jsonArray2.add(xpathList.get(i));
        }
        jsonArray.add(jsonArray2);
        job.setJobDataMap(JSON.toJSONString(jsonArray));
        job.setId(id);
        job.setJobDesc(jobDesc);
        job.setUtime(System.currentTimeMillis());
        try {
            return jobService.edit(job);
        } catch (Exception e) {
            return new Result("", false, e.getMessage());
        }
    }

    @PostMapping("/edit/v2")
    public Result<ScheduleJobVo> editV2(@RequestBody ScheduleJobVo jobVO) {
        String name = jobVO.getJobName();
        String jobDesc = jobVO.getJobDesc();
        Long id = jobVO.getId();

        if (null == id) {
            return new Result<>(new ScheduleJobVo(), false, "id should be passed when update");
        }

        String url = jobVO.getUrl();
        List<String> xpathList = jobVO.getXpathList();
        if (StringUtils.isBlank(url)) {
            return new Result(null, false, "param invalid");
        }
        if (null != xpathList && !xpathList.isEmpty()) {
            return edit(jobVO);
        }
        ScheduleJobVo job = new ScheduleJobVo();
        job.setJobName(name);
        job.setBeanClass("crawlService");
        job.setMethodName("seleniumCrawlHtmlAndSave");
        job.setJobType("temp");
        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(url);
        job.setId(id);
        job.setJobDataMap(JSON.toJSONString(jsonArray));
        job.setJobDesc(jobDesc);
        job.setCtime(System.currentTimeMillis());
        job.setUtime(System.currentTimeMillis());
        Result result;
        try {
            result = jobService.edit(job);
        } catch (Exception e) {
            return new Result("", false, e.getMessage());
        }
        return result;

    }

    @GetMapping("/start/{id}")
    public Result<String> start(@PathVariable("id") Long id) throws SchedulerException {
        jobService.start(id);
        return Result.of("启动定时任务成功");
    }

    @GetMapping("/startTemp/{id}")
    public Result<String> startTemp(@PathVariable("id") Long id) throws Exception {
        jobService.startTemp(id);
        return Result.of("启动任务成功");
    }

    @GetMapping("/state/{id}")
    public Result<String> state(@PathVariable("id") Long id) throws SchedulerException {
        return Result.of(jobService.status(id));
    }

    @GetMapping("/status/{id}")
    public Result<Integer> status(@PathVariable("id") Long id) throws SchedulerException {
        return Result.of(jobService.fetchStatus(id));
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