package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.*;
import cn.cnki.spider.util.CronUtil;
import cn.cnki.spider.util.ExcelExport;
import cn.cnki.spider.util.SecurityUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        } else if (!"admin".equals(user.getUsername())) {
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
            BeanUtils.copyProperties(srcVO, scheduleJobVoNew);
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
        } else if (!"admin".equals(user.getUsername())) {
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
            BeanUtils.copyProperties(srcVO, scheduleJobVoNew);
            if ("commonCrawlV2".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按规则");
            }
            if ("seleniumCrawlHtmlAndSave".equals(scheduleJobVoNew.getMethodName())) {
                scheduleJobVoNew.setCategory("按源码");
            }
            if ("templateCrawl".equals(scheduleJobVoNew.getMethodName())) {
                String jobDataMap = scheduleJobVoNew.getJobDataMap();
                JSONArray templateJSON = JSONArray.parseArray(jobDataMap);
                Long id = Long.parseLong(templateJSON.getString(0));
                scheduleJobVoNew.setTemplateId(id);
                // 查到templateName 返回给前端展示
                Result<TemplateVO> templateVOResult = templateService.get(id);
                TemplateVO templateVO = templateVOResult.getData();
                if (null != templateVO) {
                    scheduleJobVoNew.setTemplateName(templateVO.getName());
                }
                scheduleJobVoNew.setCategory("按模板");
            }
            String newType = scheduleJobVoNew.getJobType();
            if ("temp".equals(newType)) {
                scheduleJobVoNew.setStrategyDesc("执行一次");
            } else {
                scheduleJobVoNew.setStrategyDesc(CronUtil.translateToChinese(scheduleJobVoNew.getCronExpression()));
            }

            String err = scheduleJobVoNew.getErr();
            /*if (StringUtils.isBlank(err)) {
                scheduleJobVoNew.setErr("暂无无异常信息");
            }*/
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
        String methodName = jobVo.getMethodName();

        if ("templateCrawl".equals(methodName)) {
            Criteria criteria1 = Criteria.where(HistoryDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(HistoryDO.Fields.status).is(2);
            query.addCriteria(criteria1.andOperator(criteria2));
            query.with(Sort.by(Sort.Order.desc(HistoryDO.Fields.ctime)));
            HistoryDO historyDO = mongoTemplate.findOne(query, HistoryDO.class);
            if (null == historyDO) {
                return new Result<>("", false, "最近一次的执行记录已被删除,请考虑重新执行");
            }
            String hisId = historyDO.getId();
            Criteria criteria3 = Criteria.where(ArticleDO.Fields.jobId).is(id);
            Criteria criteria4 = Criteria.where(ArticleDO.Fields.hisId).is(hisId);
            Query query2 = new Query();
            query2.addCriteria(criteria4.andOperator(criteria3));
            query2.with(Sort.by(Sort.Order.desc(ArticleDO.Fields.ctime)));
            List<ArticleDO> articleDOS = mongoTemplate.find(query2, ArticleDO.class);
            if (articleDOS.isEmpty()) {
                return new Result<>("", false, "最近一次的执行记录已被删除,请考虑重新执行");
            }
            List<JSONObject> result = articleDOS.stream().map(articleDO -> {
                JSONObject newJson = new JSONObject();
                newJson.put("date", articleDO.getDate());
                newJson.put("pageNo", articleDO.getPageNo());
                newJson.put("pageName", articleDO.getPageName());
                newJson.put("introTitle", articleDO.getIntroTitle());
                newJson.put("author", articleDO.getAuthor());
                newJson.put("title", articleDO.getTitle());
                newJson.put("subTitle", articleDO.getSubTitle());
                newJson.put("source", articleDO.getSource());
                newJson.put("zhuanBan", articleDO.getZhuanBan());
                newJson.put("property", articleDO.getProperty());
                newJson.put("content", articleDO.getContent());
                newJson.put("image", articleDO.getImage());
                newJson.put("ctime", articleDO.getCtime());
                return newJson;
            }).collect(Collectors.toList());
            return Result.of(result);
        }
        if (json.size() > 1 ) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is(jobVo.getJobType());
            query.addCriteria(criteria1.andOperator(criteria2));
            query.with(Sort.by(Sort.Order.desc(CommonHtmlDO.Fields.ctime)));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
            if (null == commonHtmlDO) {
                return new Result<>("", false, "最近一次的执行记录已被删除,请考虑重新执行");
            }
            List<JSONObject> resultJSON = commonHtmlDO.getContent();
            if (null == resultJSON || resultJSON.isEmpty()) {
                return new Result<>("", false, "最近一次的执行依据设置的规则或入参未抓取到任何结果,请尝试修改任务再次执行");
            }
            return Result.of(commonHtmlDO.getContent());
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        HtmlDO htmlDO = mongoTemplate.findOne(query, HtmlDO.class);
        if (null == htmlDO) {
            return new Result<>("", false, "最近一次的执行记录已被删除,请考虑重新执行");
        }
        String html = htmlDO.getHtml();
        if (StringUtils.isBlank(html)) {
            return new Result<>("", false, "最近一次的执行依据设置的规则或入参未抓取到任何结果,请尝试修改任务再次执行");
        }
        return Result.of(htmlDO.getHtml());
    }

    /*@GetMapping(value = "/queryHis/{id}")
    public Result<Object> listHis(@PathVariable("id") Long id, HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isAnyBlank(page, rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        int pageNo = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is(jobVo.getJobType());
            query.addCriteria(criteria1.andOperator(criteria2));
            query.with(Sort.by(Sort.Order.desc(CommonHtmlDO.Fields.utime)));
            query.skip(pageSize * (pageNo - 1));
            query.limit(pageSize);
            List<CommonHtmlDO> commonHtmlDOList = mongoTemplate.find(query, CommonHtmlDO.class);
            Query query2 = new Query();
            query2.addCriteria(criteria1);
            long count = mongoTemplate.count(query2, CommonHtmlDO.class);
//            List<List<JSONObject>> results = commonHtmlDOList.stream().map(CommonHtmlDO::getContent).collect(Collectors.toList());
            JSONObject result = new JSONObject();
            result.put("rows", commonHtmlDOList);
            result.put("records", count);
            return Result.of(result);
        }
        Criteria criteria1 = Criteria.where(HtmlDO.Fields.jobId).is(id);
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.type).is("temp");
        query.addCriteria(criteria1.andOperator(criteria2));
        query.with(Sort.by(Sort.Order.desc(CommonHtmlDO.Fields.utime)));
        query.skip(pageSize * (pageNo - 1));
        query.limit(pageSize);
        List<HtmlDO> htmlDOList = mongoTemplate.find(query, HtmlDO.class);
//        List<String> htmlStrings = htmlDOList.stream().map(HtmlDO::getHtml).collect(Collectors.toList());
        Query query3 = new Query();
        query3.addCriteria(criteria1);
        long count = mongoTemplate.count(query3, HtmlDO.class);
        JSONObject result = new JSONObject();
        result.put("rows", htmlDOList);
        result.put("records", count);
        return Result.of(result);
    }*/

    @GetMapping(value = "/queryHis/{id}")
    public Result<Object> listHisV2(@PathVariable("id") Long id, HttpServletRequest request) {
        String page = request.getParameter("page");
        String rows = request.getParameter("rows");
        if (StringUtils.isAnyBlank(page, rows)) {
            return new Result("", false, "paginition parameter should be passed");
        }
        int pageNo = Integer.parseInt(page);
        int pageSize = Integer.parseInt(rows);
        Query query = new Query();
        Criteria criteria1 = Criteria.where(HistoryDO.Fields.jobId).is(id);
        query.addCriteria(criteria1);
        query.with(Sort.by(Sort.Order.desc(HistoryDO.Fields.utime)));
        query.skip(pageSize * (pageNo - 1));
        query.limit(pageSize);
        List<HistoryDO> historyDOList = mongoTemplate.find(query, HistoryDO.class);
        if (!historyDOList.isEmpty()) {
            historyDOList = historyDOList.stream().map(historyDO -> {
                String err = historyDO.getErr();
                if (StringUtils.isBlank(err)) {
                    historyDO.setErr("本次执行未出现异常情况");
                }
                return historyDO;
            }).collect(Collectors.toList());
        }
        Query query2 = new Query();
        query2.addCriteria(criteria1);
        long count = mongoTemplate.count(query2, HistoryDO.class);

        JSONObject result = new JSONObject();
        result.put("rows", historyDOList);
        result.put("records", count);
        return Result.of(result);
    }


    @GetMapping(value = "/his/del/{id}")
    public Result<Object> deleteHis(@PathVariable("id") String id, HttpServletRequest request) {
        if (StringUtils.isBlank(id)) {
            return new Result("", false, "his id should be passed");
        }
        Query query = new Query();
        Criteria criteria = Criteria.where(HistoryDO.Fields.id).is(id);
        query.addCriteria(criteria);
        mongoTemplate.remove(query, HistoryDO.class);
        return Result.of(id);
    }

    /*@GetMapping(value = "/queryHis/show/{id}")
    public Result<Object> listHisShow(@PathVariable("id") String id) {

        Query query = new Query();
        Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.id).is(id);
        query.addCriteria(criteria1);
        CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
        if (null != commonHtmlDO) {
            return Result.of(commonHtmlDO.getContent());
        }
        Query query2 = new Query();
        Criteria criteria2 = Criteria.where(HtmlDO.Fields.id).is(id);
        query2.addCriteria(criteria2);
        HtmlDO htmlDO = mongoTemplate.findOne(query2, HtmlDO.class);
//        List<String> htmlStrings = htmlDOList.stream().map(HtmlDO::getHtml).collect(Collectors.toList());
        if (null == htmlDO) {
            return new Result("", false, "数据不存在");
        }
        return Result.of(htmlDO.getHtml());
    }*/

    @GetMapping(value = "/queryHis/show/{id}")
    public Result<Object> listHisShowV2(@PathVariable("id") String id) {

        Query query = new Query();
        Criteria criteria1 = Criteria.where(HistoryDO.Fields.id).is(id);
        query.addCriteria(criteria1);
        HistoryDO historyDO = mongoTemplate.findOne(query, HistoryDO.class);
        if (null == historyDO) {
            return new Result<>("", false, "本条执行记录不存在或已删除");
        }
        Integer status = historyDO.getStatus();
        if (2 != status) {
            return new Result<>("", false, "本条执行记录不存在执行结果,可能执行失败或正在执行过程中");
        }
        Long jobId = historyDO.getJobId();
        ScheduleJobVo jobVo = jobService.get(jobId).getData();
        String method = jobVo.getMethodName();
        // html源码表查询
        if ("seleniumCrawlHtmlAndSave".equals(method)) {
            // 源码爬取
            Query querySrc = new Query();
            Criteria criteriaSrc = Criteria.where(HtmlDO.Fields.hisId).is(id);
            Criteria criteriaSrc2 = Criteria.where(HtmlDO.Fields.jobId).is(jobId);
            querySrc.addCriteria(criteriaSrc.andOperator(criteriaSrc2));
            HtmlDO htmlDOResult = mongoTemplate.findOne(querySrc, HtmlDO.class);
            if (null == htmlDOResult) {
                return new Result<>("", false, "本条执行结果不存在或已经被管理员删除");
            }
            return Result.of(htmlDOResult.getHtml());
        } else if ("commonCrawlV2".equals(method)) {
            // xpath规则爬取
            Query querySrc = new Query();
            Criteria criteriaSrc = Criteria.where(CommonHtmlDO.Fields.hisId).is(id);
            Criteria criteriaSrc2 = Criteria.where(CommonHtmlDO.Fields.jobId).is(jobId);
            querySrc.addCriteria(criteriaSrc.andOperator(criteriaSrc2));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(querySrc, CommonHtmlDO.class);
            if (null == commonHtmlDO) {
                return new Result<>("", false, "本条执行结果不存在或已经被管理员删除");
            }
            List<JSONObject> content = commonHtmlDO.getContent();
            if (null == content || content.isEmpty()) {
                return new Result<>("", false, "本次执行依据传入传入的xpath规则未抓到任何数据,请检查规则");
            }
            return Result.of(commonHtmlDO.getContent());
        } else if ("templateCrawl".equals(method)) {
                String hisId = historyDO.getId();
                Criteria criteria3 = Criteria.where(ArticleDO.Fields.jobId).is(jobId);
                Criteria criteria4 = Criteria.where(ArticleDO.Fields.hisId).is(hisId);
                Query query2 = new Query();
                query2.addCriteria(criteria4.andOperator(criteria3));
                query2.with(Sort.by(Sort.Order.desc(ArticleDO.Fields.ctime)));
                List<ArticleDO> articleDOS = mongoTemplate.find(query2, ArticleDO.class);
                if (articleDOS.isEmpty()) {
                    return new Result<>("", false, "最近一次的执行记录已被删除,请考虑重新执行");
                }
            List<JSONObject> result = articleDOS.stream().map(articleDO -> {
                JSONObject newJson = new JSONObject();
                newJson.put("date", articleDO.getDate());
                newJson.put("pageNo", articleDO.getPageNo());
                newJson.put("pageName", articleDO.getPageName());
                newJson.put("introTitle", articleDO.getIntroTitle());
                newJson.put("author", articleDO.getAuthor());
                newJson.put("title", articleDO.getTitle());
                newJson.put("subTitle", articleDO.getSubTitle());
                newJson.put("source", articleDO.getSource());
                newJson.put("zhuanBan", articleDO.getZhuanBan());
                newJson.put("property", articleDO.getProperty());
                newJson.put("content", articleDO.getContent());
                newJson.put("image", articleDO.getImage());
                newJson.put("ctime", articleDO.getCtime());
                return newJson;
            }).collect(Collectors.toList());
            return Result.of(result);
        }

        return Result.of("本次执行未抓取到任何数据");
    }

//    @GetMapping(value = "/queryHis/export/{id}")
//    public void listHisExport(@PathVariable("id") String id, HttpServletResponse response)
//            throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {
//
//        Query query = new Query();
//        Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.id).is(id);
//        query.addCriteria(criteria1);
//        CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
//
//        if (commonHtmlDO != null) {
//            ExcelExport excelExport = new ExcelExport(response, "导出结果", "导出结果");
//            List<JSONObject> jsonArr = commonHtmlDO.getContent();
//            JSONObject newJson = jsonArr.get(0);
//            Set<String> head = newJson.keySet();
//            List<String> newList = Lists.newArrayList(head);
//            excelExport.writeExcel(newList.toArray(new String[head.size()]),
//                    newList.toArray(new String[head.size()]), jsonArr);
//            return;
//        }
//        Query query2 = new Query();
//        Criteria criteria2 = Criteria.where(HtmlDO.Fields.id).is(id);
//        query2.addCriteria(criteria2);
//        HtmlDO htmlDO = mongoTemplate.findOne(query2, HtmlDO.class);
//        if (null == htmlDO) {
//            response.getWriter().write("there's no data for this job");
//            return;
//        }
//        response.setContentType("application/txt");// 设置文本内省
//        response.setCharacterEncoding("utf-8");// 设置字符编码
//        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("导出结果", "UTF-8") + ".txt"); // 设置响应头
//        response.getWriter().write(htmlDO.getHtml());
//    }

    @GetMapping(value = "/queryHis/export/{id}")
    public void listHisExportV2(@PathVariable("id") String id, HttpServletResponse response)
            throws NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException {

        Result<Object> resultData = listHisShowV2(id);
        Object data = resultData.getData();
        if (data instanceof List) {
            ExcelExport excelExport = new ExcelExport(response, "导出结果", "导出结果");
            List<JSONObject> jsonArr = (List<JSONObject>)data;
            JSONObject newJson = jsonArr.get(0);
            Set<String> head = newJson.keySet();
            List<String> newList = Lists.newArrayList(head);
            excelExport.writeExcel(newList.toArray(new String[head.size()]),
                    newList.toArray(new String[head.size()]), jsonArr);
            return;
        }else if (data instanceof String) {
            response.setContentType("application/txt");// 设置文本内省
            response.setCharacterEncoding("utf-8");// 设置字符编码
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("导出结果", "UTF-8") + ".txt"); // 设置响应头
            response.getWriter().write(String.valueOf(data));
        }
    }

    @GetMapping(value = "/export/{id}")
    public void export(@PathVariable("id") Long id, HttpServletResponse response) throws Exception {

        /*Query query = new Query();
        ScheduleJobVo jobVo = jobService.get(id).getData();
        String dataMap = jobVo.getJobDataMap();
        JSONArray json = JSONArray.parseArray(dataMap);
        if (json.size() > 1) {
            Criteria criteria1 = Criteria.where(CommonHtmlDO.Fields.jobId).is(id);
            Criteria criteria2 = Criteria.where(CommonHtmlDO.Fields.type).is(jobVo.getJobType());
            query.addCriteria(criteria1.andOperator(criteria2));
            CommonHtmlDO commonHtmlDO = mongoTemplate.findOne(query, CommonHtmlDO.class);
            ExcelExport excelExport = new ExcelExport(response, "导出结果", "导出结果");
            List<JSONObject> jsonArr = commonHtmlDO.getContent();
            JSONObject newJson = jsonArr.get(0);
            Set<String> head = newJson.keySet();
            List<String> newList = Lists.newArrayList(head);
            excelExport.writeExcel(newList.toArray(new String[head.size()]),
                    newList.toArray(new String[head.size()]), jsonArr);
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
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("导出结果", "UTF-8") + ".txt"); // 设置响应头
        response.getWriter().write(htmlDO.getHtml());*/

        Result<Object> resultData = list(id);
        Object data = resultData.getData();
        if (data instanceof List) {
            ExcelExport excelExport = new ExcelExport(response, "导出结果", "导出结果");
            List<JSONObject> jsonArr = (List<JSONObject>)data;
            JSONObject newJson = jsonArr.get(0);
            Set<String> head = newJson.keySet();
            List<String> newList = Lists.newArrayList(head);
            excelExport.writeExcel(newList.toArray(new String[head.size()]),
                    newList.toArray(new String[head.size()]), jsonArr);
            return;
        }else if (data instanceof String) {
            response.setContentType("application/txt");// 设置文本内省
            response.setCharacterEncoding("utf-8");// 设置字符编码
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode("导出结果", "UTF-8") + ".txt"); // 设置响应头
            response.getWriter().write(String.valueOf(data));
        }
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

    /*@PostMapping("/add/v2")
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
    }*/

    @PostMapping("/add/v2")
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
            jsonArray.add(templateId);
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

    @GetMapping("html")
    public ModelAndView html(HttpServletRequest request) {
        String jobId = request.getParameter("jobId");
        String hisId = request.getParameter("hisId");
        if (StringUtils.isBlank(jobId) && StringUtils.isBlank(hisId)) {
            return new ModelAndView("common/error/404.html");
        }
        if (StringUtils.isNotBlank(jobId)){
            Long jobIdLong = Long.parseLong(jobId);
            Result<Object> result = list(jobIdLong);
            Object data = result.getData();
            if (data instanceof String) {
                return new ModelAndView("common.html", "htmlcontent", data);
            }
            return new ModelAndView("common/error/404.html");
        }

        Result<Object> result = listHisShowV2(hisId);
        Object data = result.getData();
        if (data instanceof String) {
            return new ModelAndView("common.html", "htmlcontent", data);
        }
        return new ModelAndView("common/error/404.html");
    }

}