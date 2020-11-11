package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.pojo.HtmlDO;
import cn.cnki.spider.common.pojo.PageInfo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.util.CronUtil;
import cn.cnki.spider.util.ExcelExport;
import cn.cnki.spider.util.ExcelUtil;
import cn.cnki.spider.util.SecurityUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/job")
@RequiredArgsConstructor
public class ScheduleJobController {

    private final ScheduleJobService jobService;

    private final TemplateService templateService;

    private final MongoTemplate mongoTemplate;

    @GetMapping(value = "/list")
    public Result<PageInfo<ScheduleJobVoNew>> list(HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isBlank(page) || StringUtils.isBlank(rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        ScheduleJobVo vo = new ScheduleJobVo();
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            vo.setLoginName("sa");
        } else if (!"admin".equals(user.getUsername())){
            vo.setLoginName(user.getUsername());
        }
        vo.setPage(Integer.parseInt(page));
        vo.setRows(Integer.parseInt(rows));
        Result<PageInfo<ScheduleJobVo>> pages = jobService.page(vo);
        List<ScheduleJobVo> vos = pages.getData().getRows();
        PageInfo<ScheduleJobVoNew> jobNewPage = new PageInfo<>();
        BeanUtils.copyProperties(pages.getData(), jobNewPage);
        if (null == vos || vos.isEmpty()) {
            jobNewPage.setRows(Collections.emptyList());
            return Result.of(jobNewPage);
        }
        List<ScheduleJobVoNew> newVOS = vos.stream().map(srcVO -> {
            ScheduleJobVoNew scheduleJobVoNew = new ScheduleJobVoNew();
            BeanUtils.copyProperties(srcVO,  scheduleJobVoNew);
            if ("seleniumCrawlHtmlAndSave".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按源码");
            }
            if ("commonCrawlV2".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按源码");
            }
            if ("templateCrawl".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按模板");
            }
            String type = scheduleJobVoNew.getJobType();
            if ("temp".equals(type)) {
                scheduleJobVoNew.setStrategyDesc("执行一次");
            } else {
                scheduleJobVoNew.setStrategyDesc(CronUtil.translateToChinese(scheduleJobVoNew.getCronExpression()));
            }

            String err = scheduleJobVoNew.getErr();
            if (StringUtils.isBlank(err)) {
                scheduleJobVoNew.setErr("暂无无异常信息");
            }
            return scheduleJobVoNew;
        }).collect(Collectors.toList());
        jobNewPage.setRows(newVOS);
        return Result.of(jobNewPage);
    }

    @GetMapping(value = "/list/v2")
    public Result<PageInfo<ScheduleJobVoNew>> listV2(Integer type, HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isBlank(page) || StringUtils.isBlank(rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        ScheduleJobVo vo = new ScheduleJobVo();
        if ("1".equals(String.valueOf(type))) {
            vo.setJobStatus("1");
        } else if ("2".equals(String.valueOf(type))) {
            vo.setJobStatus("2");
        } else if ("3".equals(String.valueOf(type))) {
            vo.setJobStatus("2");
        }
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            vo.setLoginName("sa");
        } else if (!"admin".equals(user.getUsername())){
            vo.setLoginName(user.getUsername());
        }
        vo.setPage(Integer.parseInt(page));
        vo.setRows(Integer.parseInt(rows));
        Result<PageInfo<ScheduleJobVo>> pages = jobService.page(vo);
        List<ScheduleJobVo> vos = pages.getData().getRows();
        PageInfo<ScheduleJobVoNew> jobNewPage = new PageInfo<>();
        BeanUtils.copyProperties(pages.getData(), jobNewPage);
        if (null == vos || vos.isEmpty()) {
            jobNewPage.setRows(Collections.emptyList());
            return Result.of(jobNewPage);
        }
        List<ScheduleJobVoNew> newVOS = vos.stream().map(srcVO -> {
            ScheduleJobVoNew scheduleJobVoNew = new ScheduleJobVoNew();
            BeanUtils.copyProperties(srcVO,  scheduleJobVoNew);
            if ("seleniumCrawlHtmlAndSave".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按源码");
            }
            if ("seleniumCrawlHtmlAndSave".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按源码");
            }
            if ("templateCrawl".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按模板");
            }
            String newType = scheduleJobVoNew.getJobType();
            if ("temp".equals(newType)) {
                scheduleJobVoNew.setStrategyDesc("执行一次");
            } else {
                scheduleJobVoNew.setStrategyDesc(CronUtil.translateToChinese(scheduleJobVoNew.getCronExpression()));
            }

            String err = scheduleJobVoNew.getErr();
            if (StringUtils.isBlank(err)) {
                scheduleJobVoNew.setErr("暂无无异常信息");
            }
            return scheduleJobVoNew;
        }).collect(Collectors.toList());
        jobNewPage.setRows(newVOS);
        return Result.of(jobNewPage);
    }

    @GetMapping(value = "/template/list")
    public Result<PageInfo<TemplateVO>> listTemplate(HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isBlank(page) || StringUtils.isBlank(rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        TemplateVO vo = new TemplateVO();
        vo.setPage(Integer.parseInt(page));
        vo.setRows(Integer.parseInt(rows));
        Result<PageInfo<TemplateVO>> pages = templateService.page(vo);
        return pages;
    }

    @GetMapping(value = "/query/{id}")
    public Result<Object> list(@PathVariable("id") Long id) {

        Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is(jobVo.getJobType());
            query.addCriteria(criteria1.andOperator(criteria2));
            query.with(Sort.by(Sort.Order.desc(CommonHtmlDO.Fields.ctime)));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
            if (null == commonHtmlDO) {
                return Result.of(Collections.EMPTY_LIST);
            }
            return Result.of(commonHtmlDO.getContent());
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        HtmlDO htmlDO = mongoTemplate.findOne(query, HtmlDO.class);
        return Result.of(htmlDO.getHtml());
    }

    @GetMapping(value = "/queryHis/{id}")
    public Result<Object> listHis(@PathVariable("id") Long id) {

        Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is(jobVo.getJobType());
            query.addCriteria(criteria1.andOperator(criteria2));
            List<CommonHtmlDO> commonHtmlDOList = mongoTemplate.find(query, CommonHtmlDO.class);
            List<List<JSONObject>> results = commonHtmlDOList.stream().map(CommonHtmlDO::getContent).collect(Collectors.toList());
            return Result.of(results);
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        List<HtmlDO> htmlDOList = mongoTemplate.find(query, HtmlDO.class);
        List<String> htmlStrings = htmlDOList.stream().map(HtmlDO::getHtml).collect(Collectors.toList());
        return Result.of(htmlStrings);
    }

    @GetMapping(value = "/export/{id}")
    public void export(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {

        Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is("temp");
            query.addCriteria(criteria1.andOperator(criteria2));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
            ExcelExport excelExport = new ExcelExport(response, "导出结果", "导出结果");
            List<JSONObject> jsonArr = commonHtmlDO.getContent();
            JSONObject newJson = jsonArr.get(0);
            Set<String> head = newJson.keySet();
            List<String> newList = Lists.newArrayList(head);
            excelExport.writeExcel(newList.toArray(new String [head.size()]),
                    newList.toArray(new String [head.size()]), jsonArr);
            return;
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        HtmlDO htmlDO = mongoTemplate.findOne(query, HtmlDO.class);
        if (null == htmlDO) {
            response.getWriter().write("there's no data for this job");
            return;
        }
        response.setContentType("application/txt");// 设置文本内省
        response.setCharacterEncoding("utf-8");// 设置字符编码
        response.setHeader("Content-disposition", "attachment;filename="+ URLEncoder.encode("导出结果","UTF-8") + ".txt"); // 设置响应头
        response.getWriter().write(htmlDO.getHtml());
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
        String jobType = jobVO.getJobType();
        if (StringUtils.isBlank(jobType)) {
            job.setJobType("temp");
        } else {
            job.setJobType(jobType);
        }

        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        } else {
            job.setCronExpression(cron);
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
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            job.setLoginName("sa");
        } else {
            job.setLoginName(user.getUsername());
        }
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
        if (null != xpathList && !xpathList.isEmpty() && !StringUtils.isBlank(xpathList.get(0))) {
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
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            job.setLoginName("sa");
        } else {
            job.setLoginName(user.getUsername());
        }
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

    @PostMapping("/add/v3")
    public Result<String> addV2(@RequestBody ScheduleJobVoNew jobVO) {
        String name = jobVO.getJobName();
        String jobDesc = jobVO.getJobDesc();
        String url = jobVO.getUrl();
        List<String> xpathList = jobVO.getXpathList();
        Long templateId = jobVO.getTemplateId();
        // 按模板
        if (null != templateId) {
            boolean isTemplateByDate = jobVO.isTemplateByDate();
            String jobType = jobVO.getJobType();
            boolean skipOnErr = jobVO.isSkipOnErr();
            ScheduleJobVo job = new ScheduleJobVo();
            BeanUtils.copyProperties(jobVO, job);
            job.setJobName(name);
            job.setBeanClass("crawlService");
            job.setMethodName("templateCrawl");
            job.setJobType(jobType);
            String cron = job.getCronExpression();
            if (StringUtils.isBlank(cron)) {
                job.setCronExpression("0/2 * * * * ? 2030");
            } else {
                job.setCronExpression("0 0 23 * * ?");
            }
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(url);
            job.setJobDataMap(JSON.toJSONString(jsonArray));
            User user = SecurityUtil.getLoginUser();
            if (null == user) {
                job.setLoginName("sa");
            } else {
                job.setLoginName(user.getUsername());
            }
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
        if (StringUtils.isBlank(url)) {
            return new Result(null, false, "param invalid");
        }

        // 爬取html源码
        if (null != xpathList && !xpathList.isEmpty() && !StringUtils.isBlank(xpathList.get(0))) {
            ScheduleJobVo vo = new ScheduleJobVo();
            BeanUtils.copyProperties(jobVO, vo);
            String jobType = jobVO.getJobType();
            boolean skipOnErr = jobVO.isSkipOnErr();
            vo.setCronExpression(jobType);
            return add(vo);
        }
        ScheduleJobVo job = new ScheduleJobVo();
        job.setJobName(name);
        job.setBeanClass("crawlService");
        job.setMethodName("seleniumCrawlHtmlAndSave");
        String jobType = jobVO.getJobType();
        if (StringUtils.isBlank(jobType)) {
            job.setJobType("temp");
        } else {
            job.setJobType(jobType);
        }

        String cron = job.getCronExpression();
        if (StringUtils.isBlank(cron)) {
            job.setCronExpression("0/2 * * * * ? 2030");
        } else {
            job.setCronExpression(cron);
        }
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(url);
        job.setJobDataMap(JSON.toJSONString(jsonArray));
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            job.setLoginName("sa");
        } else {
            job.setLoginName(user.getUsername());
        }
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
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            job.setLoginName("sa");
        } else {
            job.setLoginName(user.getUsername());
        }
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
        if (null != xpathList && !xpathList.isEmpty() && !StringUtils.isBlank(xpathList.get(0))) {
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
        User user = SecurityUtil.getLoginUser();
        if (null == user) {
            job.setLoginName("sa");
        } else {
            job.setLoginName(user.getUsername());
        }
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