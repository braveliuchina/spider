package cn.cnki.spider.common.service;

import cn.cnki.spider.common.pojo.ArticleDO;
import cn.cnki.spider.common.pojo.HistoryDO;
import cn.cnki.spider.common.pojo.HtmlDO;
import cn.cnki.spider.common.repository.CrawlHtmlRepository;
import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.*;
import cn.cnki.spider.pipeline.ArticleMongoDBBatchPageModelPipeline;
import cn.cnki.spider.pipeline.CommonPageModelPipeline;
import cn.cnki.spider.pipeline.MhtFilePipeline;
import cn.cnki.spider.scheduler.ScheduleJob;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import cn.cnki.spider.spider.AbstractCloudNewspaperProcessor;
import cn.cnki.spider.spider.CommonRepoProcessor;
import cn.cnki.spider.util.ChromeUtil;
import cn.cnki.spider.util.CommonForkJoinPool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import us.codecraft.webmagic.Spider;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
@RequiredArgsConstructor
public class CrawlService implements cn.cnki.spider.common.service.CrawlServiceInterface {

    private AbstractCloudNewspaperProcessor processor;

    private final ProcessorFactory processorFactory;

    private final ArticleMongoDBBatchPageModelPipeline dbPageModelPipeline;

    private final CommonRepoProcessor commonRepoProcessor;

    private final CommonPageModelPipeline commonPageModelPipeline;

    private final ChromeUtil chromeUtil;

    private final RestTemplate restTemplate = new RestTemplate();

    private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>() {
    };
    private final CrawlHtmlRepository crawlHtmlRepository;

    private final MongoTemplate mongoTemplate;

    private final ScheduleJobRepository scheduleJobRepository;

    private final SpiderConfigDao spiderConfigDao;

    private final CommonForkJoinPool forkJoinPool = new CommonForkJoinPool(16, "crawlPool");


    private void crawl(List<String> urls, int thread) {
        Spider detailSpider = Spider.create(processor);
        if (null == urls || urls.isEmpty()) {
            return;
        }
        for (String url : urls) {
            detailSpider.addUrl(url).addPipeline(dbPageModelPipeline)
                    .thread(thread).run();
        }
    }

    private void crawl(List<ArticleDO> articles) throws Exception {
        if (null == articles || articles.isEmpty()) {
            throw new Exception("article download list is empty");
        }
        processor.setArticles(articles);
        Spider detailSpider = Spider.create(processor);
        detailSpider.addUrl("http://www.baidu.com/").addPipeline(new MhtFilePipeline("C:/spider")).addPipeline(dbPageModelPipeline).run();
    }

    @Override
    public void crawlByDate(String year, String month, String day,
                            String code, String paperName) throws Exception {

        SpiderConfig config = crawlByDateInner(year, month, day, code, paperName);

        if (null == config) {
            throw new Exception("处理异常,请联系管理员");
        }


    }

    @Override
    public void crawlByDate(String year, String month, String day, String code, String... paperName) throws Exception {
        List<Long> ids = Lists.newArrayList();
        for (int i = 0; i < paperName.length; i++) {
            processor.setCode("");
            processor.setNewspaperName(paperName[i]);
            processor.setSite(null);
            SpiderConfig config = crawlByDateInner(year, month, day, "", paperName[i]);
            if (null == config) {
                continue;
            }
            ids.add(config.getId());
        }
        String dateStr = year + month + day;
    }

    @Override
    public List<String> buildEnterUrl(String year, String month, String day,
                                      String code, String paperName) throws Exception {
        processor.getSite();
        SpiderConfig config = processor.getSpiderConfig();
        String type = config.getType();
        Site siteRule = processor.getSiteRule();

        // flash的情况尝试获取xml文件读取文章内容
        if ("flashXml".equals(type)) {
            return buildXMLEnterUrl(siteRule, config, year, month, day);
        }
        // ajax请求拿到响应读取文章内容
        if ("discoveryAjax".equals(type)) {
            // 由url发现id并用id发送ajax请求的情况
            return buildAjaxEnterUrl(type, paperName, siteRule, year, month, day, config);
        }
        // 普通html类型的网页文章内容抓取
        return buildCommonTypeUrl(type, paperName, siteRule, year, month, day, config);
    }

    @Override
    public List<String> buildEnterUrl(String year, String month, String day,
                                      String code, String... paperName) throws Exception {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < paperName.length; i++) {
            processor.setCode("");
            processor.setNewspaperName(paperName[i]);
            processor.setSite(null);
            List<String> urls = buildEnterUrl(year, month, day, "", paperName[i]);
            if (null != urls && !urls.isEmpty()) {
                result.addAll(urls);
            }
        }
        return result;
    }

    @Override
    public List<String> buildActualCrawlUrl(String year, String month, String day, String code, String paperName) throws Exception {
        processor.getSite();
        Site siteRule = processor.getSiteRule();
        boolean siteRulePageUrlDeepInto = siteRule.isPageUrlDeepInto();
        List<String> enterUrls = buildEnterUrl(year, month, day, code, paperName);
        if (!siteRulePageUrlDeepInto) {
            return enterUrls;
        }
        HtmlCleaner hc = new HtmlCleaner();
        SpiderConfig config = processor.getSpiderConfig();
        HashMap<String, Content> content = (HashMap<String, Content>) JSONObject.parseObject(config.getContent(),
                typeReference);
        Content contentArticle = content.get("article");
        String metaPrefix = contentArticle.getMetaPrefix();
        return enterUrls.stream().map(url -> {
            String html = simplePlainGetRequest(url);
            TagNode tn = hc.clean(html);
            Object[] contentNew = new Object[]{};
            try {
                contentNew = tn.evaluateXPath("//meta[@HTTP-EQUIV='REFRESH']/@CONTENT");
            } catch (XPatherException e) {
                e.printStackTrace();
            }
            if (contentNew.length <= 0) {
                return url;
            }
            if (StringUtils.isBlank(metaPrefix)) {
                return url + (((String) contentNew[0]).split(";")[1]).split("=")[1];
            }
            return metaPrefix + (((String) contentNew[0]).split(";")[1]).split("=")[1];
        }).collect(Collectors.toList());

    }

    @Override
    public List<String> buildActualCrawlUrl(String year, String month, String day, String code, String... paperName) throws Exception {
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < paperName.length; i++) {
            processor.setCode("");
            processor.setNewspaperName(paperName[i]);
            processor.setSite(null);
            List<String> urls = buildActualCrawlUrl(year, month, day, "", paperName[i]);
            if (null != urls && !urls.isEmpty()) {
                result.addAll(urls);
            }
        }
        return result;
    }

    @Override
    public void seleniumCrawl(String url) {

        try {
            File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
            System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());
            WebDriver webDriver;
            ChromeOptions options = new ChromeOptions();
            webDriver = new ChromeDriver(options);
            webDriver.manage().window().setSize(new Dimension(1300, 800));
            webDriver.get(url);
            // 等待已确保页面正常加载
            Thread.sleep(15 * 1000L);
            WebElement viewAll = webDriver.findElement(By.linkText("View All"));
            viewAll.click();
            Thread.sleep(35 * 1000L);
            WebElement table_element = webDriver.findElement(By.xpath("//div[@class='modContent']/table/tbody"));
            ArrayList<WebElement> rows = (ArrayList<WebElement>) table_element.findElements(By.tagName("tr"));
            List<AACSB> results = Lists.newArrayList();
            for (WebElement row : rows) {
                ArrayList<WebElement> cells = (ArrayList<WebElement>) row.findElements(By.tagName("td"));
                AACSB aacsb = new AACSB();
                String accreditation = "";
                for (int i = 0; i < cells.size(); i++) {
                    WebElement cell = cells.get(i);
                    if (0 == i) {
                        String university = cell.findElement(By.tagName("b")).getText();
                        String region = cell.findElement(By.tagName("span")).getText();
                        String school = cell.findElement(By.tagName("p")).getText();
                        aacsb.setUniversity(university);
                        aacsb.setRegion(region);
                        aacsb.setAcademy(school);
                        continue;
                    }
                    if (1 == i) {
                        try {
                            cell.findElement(By.tagName("img"));
                            accreditation = "Business";
                        } catch (org.openqa.selenium.NoSuchElementException e) {
                            continue;
                        }
                    }
                    if (2 == i) {
                        try {
                            cell.findElement(By.tagName("img"));
                            accreditation = StringUtils.isBlank(accreditation) ?
                                    "Accounting" : accreditation + ",Accounting";
                        } catch (org.openqa.selenium.NoSuchElementException e) {
                            continue;
                        }
                    }
                    aacsb.setAccreditation(accreditation);
                }
                results.add(aacsb);
            }
//            aacsbDao.batchInsert(results);
            webDriver.quit();
        } catch (Exception e) {
            log.warn("AACSB crawl exception err", e);
        }
    }

    @Override
    public String seleniumCrawlHtml(String url) {
        WebDriver webDriver = null;
        try {
            File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
            System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

            ChromeOptions options = new ChromeOptions();
            webDriver = new ChromeDriver(options);
            webDriver.manage().window().setSize(new Dimension(1300, 800));
            webDriver.get(url);
            // 等待已确保页面正常加载
            Thread.sleep(15 * 1000L);
            return webDriver.getPageSource();
        } catch (Exception e) {
            log.warn("AACSB crawl exception err", e);
        } finally {
            if (null != webDriver) {
                webDriver.quit();
            }
        }
        return "";
    }

    @Override
    public void seleniumCrawlHtmlAndSave(long jobId, String type, String hisId, String url) {
        String htmlContent = "";
        String err = "";
        try {
            htmlContent = seleniumCrawlHtml(url);
        } catch (Exception e) {
            err = e.getMessage();
        }
        long now = System.currentTimeMillis();
        ScheduleJob job = saveAndUpdateJobAndHis(jobId, err, hisId);
        // 执行成功需要保存执行结果
        if ("2".equals(job.getJobStatus())) {
            HtmlDO htmlDO = new HtmlDO();
            htmlDO.setUrl(url);
            htmlDO.setType(job.getJobType());
            htmlDO.setHisId(hisId);
            htmlDO.setHtml(htmlContent);
            htmlDO.setJobId(jobId);
            htmlDO.setErr(err);
            htmlDO.setCtime(now);
            htmlDO.setUtime(now);
            // 任务执行结果表
            crawlHtmlRepository.save(htmlDO);
        }

    }

    @Override
    public void commonCrawl(String url, List<String> xpathList) {
        log.info("url is {}, xpath list is {}", url, xpathList);
        if (StringUtils.isBlank(url) || null == xpathList || xpathList.isEmpty()) {
            log.warn("param is null");
            return;
        }
        commonRepoProcessor.setXpathList(xpathList);
        Spider spider = Spider.create(commonRepoProcessor);
        spider.addUrl(url).addPipeline(commonPageModelPipeline)
                .thread(1).run();
    }

    @Override
    public void commonCrawlV2(long jobId, String type, String hisId, String url, List<String> xpathList) {
        log.info("url is {}, xpath list is {}", url, xpathList);
        if (StringUtils.isBlank(url) || null == xpathList || xpathList.isEmpty()) {
            log.warn("param is null");
            return;
        }
        commonRepoProcessor.setXpathList(xpathList);
        commonRepoProcessor.setJobId(jobId);
        commonRepoProcessor.setType(type);
        commonRepoProcessor.setHisId(hisId);
        Spider spider = Spider.create(commonRepoProcessor);
        spider.addUrl(url).addPipeline(commonPageModelPipeline)
                .thread(1).run();
    }

    @Override
    public void templateCrawl(long jobId, String type, String hisId, long templateId) {
        String err = "";
        SpiderConfig config = spiderConfigDao.getConfigById(templateId);

        if (config == null) {
            log.info("fail to get config");
            err = "无此模板";
            saveAndUpdateJobAndHis(jobId, err, hisId);
        }

        String code = config.getCode();
        String name = config.getName();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String monthStr = String.valueOf(month+1);
        String dayStr = String.valueOf(day);
        month += 1;
        if (month < 10) {
            monthStr = "0" + monthStr;
        }
        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        try {
            AbstractCloudNewspaperProcessor processor = processorFactory.buildProcessor(config.getName());
            processor.setJobId(jobId);
            processor.setHisId(hisId);
            processor.setTemplateId(templateId);
            this.setProcessor(processor);
            crawlByDate(String.valueOf(year), monthStr, dayStr, code, name);
        } catch (Exception e) {
            log.error("爬取失败,{}", e);
            err = e.getMessage();
        }
        saveAndUpdateJobAndHis(jobId, err, hisId);
    }

    private ScheduleJob saveAndUpdateJobAndHis(long jobId, String err, String hisId) {
        long now = System.currentTimeMillis();
        Optional<ScheduleJob> jobOptional = scheduleJobRepository.findById(jobId);
        if (!jobOptional.isPresent()) {
            try {
                throw new Exception("there is no job with this id");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ScheduleJob job = jobOptional.get();
        Integer hisCount = job.getHis();
        // 异常情况
        if (StringUtils.isNotBlank(err)) {
            // 停止但执行失败
            job.setJobStatus("0");
            err = err.substring(0, 1000);
            job.setErr(err);

        } else {
            // 停止但执行成功
            job.setJobStatus("2");
            job.setErr("");
            Integer result = job.getResult();
            if (null == result) {
                job.setResult(1);
            } else {
                job.setResult(++result);
            }
        }
        if (null == hisCount) {
            job.setHis(1);
        } else {
            job.setHis(++hisCount);
        }
        job.setUtime(System.currentTimeMillis());
        // 任务表
        scheduleJobRepository.saveAndFlush(job);

        // 更新任务执行记录表状态
        HistoryDO historyDO = new HistoryDO();
        historyDO.setId(hisId);
        historyDO.setJobId(job.getId());
        historyDO.setStatus(Integer.parseInt(job.getJobStatus()));
        historyDO.setErr(err);
        historyDO.setCtime(now);
        historyDO.setUtime(now);
        mongoTemplate.save(historyDO);
        return job;
    }

    private String buildActualIntMonthOrDay(String dateStr) {
        if (dateStr.startsWith("0")) {
            return dateStr.substring(dateStr.indexOf("0") + 1);
        }
        return dateStr;
    }

    private boolean existsDay(String response, String year, String month, String day) {
        JSONObject dayJSON = JSON.parseObject(response, JSONObject.class);
        if (!dayJSON.containsKey("response")) {
            return false;
        }
        JSONObject responseJSON = (JSONObject) dayJSON.get("response");
        if (!responseJSON.containsKey(year)) {
            return false;
        }
        JSONArray yearJSON = (JSONArray) responseJSON.get(year);
        String intMonth = buildActualIntMonthOrDay(month);
        JSONObject yearObject = (JSONObject) yearJSON.get(0);
        if (!yearObject.containsKey(intMonth)) {
            return false;
        }
        List<Integer> days = (List<Integer>) yearObject.get(intMonth);
        if (null == days || days.isEmpty()) {
            return false;
        }
        return days.contains(Integer.parseInt(buildActualIntMonthOrDay(day)));
    }

    private Map<String, List<String>> buildArticleIdContent(String articleIdResponse) {
        if (StringUtils.isBlank(articleIdResponse)) {
            return null;
        }
        JSONObject responseJSON = JSON.parseObject(articleIdResponse, JSONObject.class);
        if (!responseJSON.containsKey("response")) {
            return null;
        }
        JSONObject articleJSON = (JSONObject) responseJSON.get("response");
        if (articleJSON.isEmpty()) {
            return null;
        }
        Map<String, List<String>> resultMap = Maps.newHashMap();
        for (String key : articleJSON.keySet()) {
            JSONObject json = (JSONObject) articleJSON.get(key);
            List<String> articleIds = Lists.newArrayList();
            if (!json.containsKey("area")) {
                resultMap.put(key, articleIds);
                continue;
            }
            String area = (String) json.get("area");
            JSONArray areaJSON = JSONArray.parseArray(area);
            for (Object item : areaJSON) {
                Long articleId = (Long) ((JSONObject) item).get("z");
                articleIds.add(String.valueOf(articleId));
            }
            resultMap.put(key, articleIds);
        }
        return resultMap;
    }

    private String buildPageName(String response) {
        JSONObject json = JSON.parseObject(response, JSONObject.class);
        if (!json.containsKey("response")) {
            return "";
        }
        JSONObject banName = (JSONObject) json.get("response");
        if (!banName.containsKey("name")) {
            return "";
        }
        return (String) banName.get("name");
    }

    private Integer buildPageId(String response) {
        JSONObject json = JSON.parseObject(response, JSONObject.class);
        if (!json.containsKey("response")) {
            return null;
        }
        JSONObject pageId = (JSONObject) json.get("response");
        if (!pageId.containsKey("issue")) {
            return null;
        }
        return (Integer) pageId.get("issue");
    }

    private ArticleDO buildArticle(String cfgId, long porotocalId,
                                       String dateStr, String pageNo, String pageName, String contentStr) {
        JSONObject json = JSON.parseObject(contentStr, JSONObject.class);
        if (!json.containsKey("response")) {
            return null;
        }
        AjaxRequestArticleContent content = JSON
                .parseObject(JSON
                        .toJSONString(((JSONArray) json.get("response")).get(0)), AjaxRequestArticleContent.class);
        ArticleDO ArticleDO = new ArticleDO();
        ArticleDO.setTemplateId(porotocalId);
        long now = System.currentTimeMillis();
        ArticleDO.setCtime(now);
        ArticleDO.setUtime(now);
        ArticleDO.setCfgId(cfgId);
        ArticleDO.setDate(dateStr);
        ArticleDO.setPageNo(pageNo);
        ArticleDO.setPageName(pageName);
        ArticleDO.setTime(content.getDate());
        ArticleDO.setTitle(content.getTitle());
        ArticleDO.setIntroTitle(content.getIntroduction());
        ArticleDO.setSubTitle(content.getSubtitle());
        ArticleDO.setContent(content.getContent());
        ArticleDO.setContentAll(content.getContent());
        return ArticleDO;
    }

    public SpiderConfig crawlByDateInnerMongoDB(long jobId, String hisId,
                                                String year, String month, String day,
                                                String code, String configName) throws Exception {
        log.info("{}, {}  ...>>cron.... started", configName, year + month + day);
        processor.getSite();
        SpiderConfig config = processor.getSpiderConfig();
        String type = config.getType();
        Site siteRule = processor.getSiteRule();

        // flash的情况尝试获取xml文件读取文章内容
        if ("flashXml".equals(type)) {
            return buildFlashXmlTypeUrlAndCrawl(siteRule, config, year, month, day);
        }
        // ajax请求拿到响应读取文章内容
        if ("ajax".equals(type)) {
            String judgeExistsUrl = siteRule.getJudgeExistsUrl();
            String pageUrl = siteRule.getDiscoverPageUrl();
            String articleIdUrl = siteRule.getDiscoverArticleId();
            String dayResponse = simplePlainGetRequest(buildUrl(judgeExistsUrl, year, month, day));
            boolean containsDay = existsDay(dayResponse, year, month, day);
            if (!containsDay) {
                throw new Exception("there is no newspaper for this day found, please check");
            }
            String pageResponse = simplePlainGetRequest(buildUrl(pageUrl, year, month, day));
            String articleIdResponse = simplePlainGetRequest(buildUrl(articleIdUrl, year, month, day));
            Map<String, List<String>> articleIdListMap = buildArticleIdContent(articleIdResponse);
            if (null == articleIdListMap || articleIdListMap.isEmpty()) {
                throw new Exception("there is no article for this day, please check");
            }
            String pageNameUrl = siteRule.getDiscoverPageNameUrl();
            String contentUrl = siteRule.getDiscoverArticleContentUrl();
            List<ArticleDO> articles = Lists.newArrayList();
            for (String pageNo : articleIdListMap.keySet()) {
                String url = buildUrl(pageNameUrl, year, month, day).replace("${pageNo}", pageNo);
                String response = simplePlainGetRequest(url);
                String banName = buildPageName(response);
                Integer id = buildPageId(pageResponse);
                contentUrl = contentUrl.replace("${id}", String.valueOf(id));
                List<String> articleIds = articleIdListMap.get(pageNo);
                for (String articleId : articleIds) {
                    String newContentUrl = contentUrl.replace("${articleId}", articleId);
                    String contentResponse = simplePlainGetRequest(newContentUrl);
                    ArticleDO article = buildArticle(articleId, config.getId(), year + month + day,
                            pageNo, banName, contentResponse);
                    articles.add(article);
                }
            }
            crawl(articles);
            return config;
        }
        // ajax请求拿到响应读取文章内容
        if ("discoveryAjax".equals(type)) {
            // 由url发现id并用id发送ajax请求的情况
            return buildAjaxRequestAndCrawl(type, configName, siteRule, year, month, day, config);
        }
        // 普通html类型的网页文章内容抓取
        return buildCommonTypeUrlAndCrawl(type, configName, siteRule, year, month, day, config);
    }

    public SpiderConfig crawlByDateInner(String year, String month, String day,
                                         String code, String configName) throws Exception {
        log.info("{}, {}  ...>>cron.... started", configName, year + month + day);
        processor.getSite();
        SpiderConfig config = processor.getSpiderConfig();
        String type = config.getType();
        Site siteRule = processor.getSiteRule();

        // flash的情况尝试获取xml文件读取文章内容
        if ("flashXml".equals(type)) {
            return buildFlashXmlTypeUrlAndCrawl(siteRule, config, year, month, day);
        }
        // ajax请求拿到响应读取文章内容
        if ("ajax".equals(type)) {
            String judgeExistsUrl = siteRule.getJudgeExistsUrl();
            String pageUrl = siteRule.getDiscoverPageUrl();
            String articleIdUrl = siteRule.getDiscoverArticleId();
            String dayResponse = simplePlainGetRequest(buildUrl(judgeExistsUrl, year, month, day));
            boolean containsDay = existsDay(dayResponse, year, month, day);
            if (!containsDay) {
                throw new Exception("there is no newspaper for this day found, please check");
            }
            String pageResponse = simplePlainGetRequest(buildUrl(pageUrl, year, month, day));
            String articleIdResponse = simplePlainGetRequest(buildUrl(articleIdUrl, year, month, day));
            Map<String, List<String>> articleIdListMap = buildArticleIdContent(articleIdResponse);
            if (null == articleIdListMap || articleIdListMap.isEmpty()) {
                throw new Exception("there is no article for this day, please check");
            }
            String pageNameUrl = siteRule.getDiscoverPageNameUrl();
            String contentUrl = siteRule.getDiscoverArticleContentUrl();
            List<ArticleDO> articles = Lists.newArrayList();
            for (String pageNo : articleIdListMap.keySet()) {
                String url = buildUrl(pageNameUrl, year, month, day).replace("${pageNo}", pageNo);
                String response = simplePlainGetRequest(url);
                String banName = buildPageName(response);
                Integer id = buildPageId(pageResponse);
                contentUrl = contentUrl.replace("${id}", String.valueOf(id));
                List<String> articleIds = articleIdListMap.get(pageNo);
                for (String articleId : articleIds) {
                    String newContentUrl = contentUrl.replace("${articleId}", articleId);
                    String contentResponse = simplePlainGetRequest(newContentUrl);
                    ArticleDO article = buildArticle(articleId, config.getId(), year + month + day,
                            pageNo, banName, contentResponse);
                    articles.add(article);
                }
            }
            crawl(articles);
            return config;
        }
        // ajax请求拿到响应读取文章内容
        if ("discoveryAjax".equals(type)) {
            // 由url发现id并用id发送ajax请求的情况
            return buildAjaxRequestAndCrawl(type, configName, siteRule, year, month, day, config);
        }
        // 普通html类型的网页文章内容抓取
        return buildCommonTypeUrlAndCrawl(type, configName, siteRule, year, month, day, config);
    }

    private void loginLogic(Site siteRule) throws Exception {
        String loginUrl = siteRule.getLoginUrl();
        String userName = siteRule.getUserName();
        String password = siteRule.getPassword();
        if (StringUtils.isBlank(loginUrl) || StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
            return;
        }
        // 模拟登录
        chromeUtil.mockLogin(loginUrl, "JJGC", userName, password);
    }

    private List<String> buildXMLEnterUrl(Site siteRule, SpiderConfig config,
                                          String year, String month, String day) throws Exception {
        loginLogic(siteRule);
        String url = siteRule.getUrl();
        if (StringUtils.isBlank(url)) {
            throw new Exception("please pass the date");
        }
        url = buildUrl(url, year, month, day);

        Set<String> resultCfgUrls = Sets.newConcurrentHashSet();
        // 初始化chrome 并打开浏览器
        chromeUtil.getDriver(url, "JJGC");
        try {
            List<String> cfgStr = chromeUtil.doflashPageXmlPageRequestV3(url, "JJGC");
            if (null == cfgStr || cfgStr.isEmpty()) {
                throw new Exception("失败,具体原因请查看后台日志");
            }
            resultCfgUrls.addAll(cfgStr);
        } catch (Exception e) {
            log.info("flash xml spider fetch cfg error", e);
        }

        if (resultCfgUrls.isEmpty()) {
            log.info("经济观察报爬虫失败,请核对规则");
            chromeUtil.releaseResource();
            throw new Exception("失败,具体原因请查看后台日志");
        }
        String header = siteRule.getFlashXmlCfgAcceptHeader();
        Set<String> result = Sets.newConcurrentHashSet();
        ForkJoinTask<?> forkJoinTask = forkJoinPool.submit(() -> resultCfgUrls.parallelStream().forEach(cfgUrl -> {
            String xmlStr = StringUtils.isBlank(header) ? buildRequestStr(cfgUrl) : buildRequestStr(cfgUrl, header);
            List<String> xmlLists = ChromeUtil
                    .fetchMultiSpiderUrl(cfgUrl.substring(0, cfgUrl.lastIndexOf("/") + 1), xmlStr);
            if (null != xmlLists && !xmlLists.isEmpty()) {
                result.addAll(xmlLists);
            }
        }));
        try {
            forkJoinTask.get();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error("Exception when wait async thread return", e);
            throw new Exception("失败,具体原因请查看后台日志");
        }

        chromeUtil.releaseResource();
        if (result.isEmpty()) {
            log.info("经济观察报爬虫失败,请核对规则");
            throw new Exception("失败,具体原因请查看后台日志");
        }
        return Lists.newArrayList(result);
    }

    // flash-xml类型的文章爬取
    private SpiderConfig buildFlashXmlTypeUrlAndCrawl(Site siteRule, SpiderConfig config,
                                                      String year, String month, String day) throws Exception {
        List<String> result = buildXMLEnterUrl(siteRule, config, year, month, day);
        int thread = siteRule.getThread();
        crawl(Lists.newArrayList(result), thread);
        return config;
    }

    private List<String> buildAjaxEnterUrl(String type, String configName, Site siteRule,
                                           String year, String month, String day,
                                           SpiderConfig config) {
        String discoveryUrl = siteRule.getDiscoveryUrl();
        String discoveryType = siteRule.getDiscoveryType();

        String url = fetchIdByDiscovery(discoveryUrl, year, month, day, discoveryType, siteRule);
        String id = "";
        if (StringUtils.isBlank(url)) {
            log.info(" crawl url failed, for paper: {}", configName);
            return null;
        }
        String idRegex = siteRule.getIdRegex();
        if (StringUtils.isNotBlank(idRegex)) {
            id = buildId(idRegex, url);
        }
        // 替换url变量
        String enterUrl = buildUrl(siteRule.getUrl(), year, month, day, id);
        return Lists.newArrayList(enterUrl);
    }

    // ajax请求抓取
    private SpiderConfig buildAjaxRequestAndCrawl(String type, String configName, Site siteRule,
                                                  String year, String month, String day,
                                                  SpiderConfig config) throws Exception {
        List<String> urls = buildAjaxEnterUrl(type, configName, siteRule, year, month, day, config);
        if (null == urls || urls.isEmpty()) {
            throw new Exception("fetch ajax request error");
        }
        String enterUrl = urls.get(0);
        buildArticleFromAjaxRequest(enterUrl, siteRule, year, month, day, config.getId());
        return config;
    }

    private void buildArticleFromAjaxRequest(String url, Site siteRule,
                                             String year, String month, String day, long protocalId) throws Exception {
        String method = siteRule.getRequestMethod();
        Map<String, String> header = siteRule.getRequestHeader();
        HttpHeaders headers = new HttpHeaders();
        String ajaxMark = header.get("X-Requested-With");
        if (StringUtils.isNotBlank(ajaxMark)) {
            headers.set("X-Requested-With", ajaxMark);
        }

        HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.resolve(method), entity,
                byte[].class);

        byte[] bodyBytes = response.getBody();
        if (null == bodyBytes) {
            return;
        }
        String responseStr = new String(bodyBytes);
        if (StringUtils.isBlank(responseStr)) {
            throw new Exception("ajax request result parse err");
        }
        AjaxContentWrapper result = JSON.parseObject(responseStr, AjaxContentWrapper.class);
        AjaxContent ajaxContent = result.getInfo();
        List<AjaxBanContent> banList = ajaxContent.getBan();
        List<List<AjaxArticleContent>> articleListList = ajaxContent.getArticle();
        List<ArticleDO> articles = Lists.newArrayList();
        long now = System.currentTimeMillis();
        for (int i = 0; i < articleListList.size(); i++) {
            List<AjaxArticleContent> ajaxArticleContentList = articleListList.get(i);
            AjaxBanContent banContent = banList.get(i);
            for (AjaxArticleContent articleContent : ajaxArticleContentList) {
                String cfgId = articleContent.getId();
                String introTitle = articleContent.getLeadtitle();
                String pageNo = banContent.getVerorder();
                String pageName = banContent.getVername();
                String title = articleContent.getTitle();
                String subTitle = articleContent.getTitle1();
                String author = articleContent.getAuthor();
                String content = articleContent.getContent();
                String contentAll = articleContent.getContent();
                String time = articleContent.getSetDate();
                List<String> images = articleContent.getImage();
                String jpg = banContent.getPerverimgurl();
                String prefix = buildUrl(siteRule.getPrefix(), year, month, day);
                if (null != images && !images.isEmpty()) {
                    List<String> newImage = Lists.newArrayList();
                    for (String image : images) {
                        image = prefix + image;
                        newImage.add(image);
                    }
                    images = newImage;
                }
                jpg = prefix + jpg;
                ArticleDO article = new ArticleDO();
                article.setTemplateId(protocalId);
                article.setDate(year + month + day);
                article.setCfgId(cfgId);
                article.setPageNo(pageNo);
                article.setPageName(pageName);
                article.setTitle(title);
                article.setSubTitle(subTitle);
                article.setAuthor(author);
                article.setIntroTitle(introTitle);
                article.setContent(content);
                article.setContentAll(contentAll);
                article.setImage(StringUtils.join(images, ","));
                article.setJpg(jpg);
                article.setTime(time);
                article.setCtime(now);
                article.setUtime(now);
                articles.add(article);

            }
        }
        crawl(articles);
    }

    private List<String> buildCommonTypeUrl(String type, String configName, Site siteRule,
                                            String year, String month, String day,
                                            SpiderConfig config) throws Exception {
        String url = "";
        String id = "";
        // 需要往下遍历的情况
        if ("discoveryHtml".equals(type)) {
            String discoveryUrl = siteRule.getDiscoveryUrl();
            String discoveryType = siteRule.getDiscoveryType();

            if (StringUtils.isBlank(discoveryType)) {
                id = fetchIdByDiscovery(discoveryUrl, year + month + day);
                if (StringUtils.isBlank(id)) {
                    throw new Exception("this page not found by this date");
                }
                url = siteRule.getUrl().replace("${id}", id);
            } else {
                url = fetchIdByDiscovery(discoveryUrl, year, month, day, discoveryType, siteRule);
                if (StringUtils.isBlank(url)) {
                    log.info(" crawl url failed, for paper: {}", configName);
                    return null;
                }
                String idRegex = siteRule.getIdRegex();
                if (StringUtils.isNotBlank(idRegex)) {
                    id = buildId(idRegex, url);
                }
            }

        } else {
            url = siteRule.getUrl();
            boolean gtId = siteRule.isGtId();
            String gtIdFormat = siteRule.getGtIdFormat();
            if (gtId && StringUtils.isNotBlank(gtIdFormat)) {
                id = buildUrl(gtIdFormat, year, month, day);
            }
        }
        if (StringUtils.isBlank(url)) {
            throw new Exception("please pass the date");
        }
        // 替换变量重构规则
        String content = config.getContent();
        String contentNew = replaceContentVar(content, id, year, month, day);
        processor.rebuildRuleMap(contentNew);
        // 替换url变量
        url = buildUrl(url, year, month, day, id);

        List<String> urls = Lists.newArrayList(url);
        return urls;
    }

    // 普通html爬取
    private SpiderConfig buildCommonTypeUrlAndCrawl(String type, String configName, Site siteRule,
                                                    String year, String month, String day,
                                                    SpiderConfig config) throws Exception {
        List<String> urls = buildCommonTypeUrl(type, configName, siteRule, year, month, day, config);
        int thread = siteRule.getThread();
        crawl(urls, thread);
        return config;
    }

    private String buildRequestStr(String url) {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);

        byte[] bodyBytes = response.getBody();

        if (null == bodyBytes) {
            return "";
        }
        return new String(bodyBytes);
    }

    private String buildRequestStr(String url, String acceptHeader) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> list = new ArrayList<>();
        list.add(MediaType.valueOf(acceptHeader));
        headers.setAccept(list);
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);

        byte[] bodyBytes = response.getBody();

        if (null == bodyBytes) {
            return "";
        }
        return new String(bodyBytes);
    }

    private SpiderConfig buildUrlAndCrawlByUrl(String id, String url,
                                               Site siteRule, SpiderConfig config,
                                               String year, String month, String day) {
        // 替换变量重构规则
        String content = config.getContent();
        String contentNew = replaceContentVar(content, id, year, month, day);
        processor.rebuildRuleMap(contentNew);
        // 替换url变量
        url = buildUrl(url, year, month, day, id);

        List<String> urls = Lists.newArrayList(url);
        int thread = siteRule.getThread();
        crawl(urls, thread);
        return config;
    }

    /**
     * fetch id from url by regex
     *
     * @param idRegex 要提取的id变量对应的正则
     * @param url     要提取id变量的url
     * @return 返回id变量
     */
    private String buildId(String idRegex, String url) {
        Pattern pattern = Pattern.compile(idRegex);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String buildUrl(String url, String year, String month, String day) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        url = url.replace("${yyyy}", year);
        url = url.replace("${mm}", month);
        url = url.replace("${dd}", day);
        return url;
    }

    private String buildUrl(String url, String year, String month, String day, String id) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        url = url.replace("${yyyy}", year);
        url = url.replace("${mm}", month);
        url = url.replace("${dd}", day);
        url = url.replace("${id}", id);
        return url;
    }

    private String replaceContentVar(String content, String id, String year, String month, String day) {
        if (StringUtils.isBlank(content)) {
            return "";
        }
        if (content.contains("${id}") && StringUtils.isNoneBlank(id)) {
            content = content.replaceAll("\\$\\{id}", id);

        }
        if (content.contains("${yyyy}") && StringUtils.isNoneBlank(year)) {
            content = content.replaceAll("\\$\\{yyyy}", year);
        }
        if (content.contains("${mm}") && StringUtils.isNoneBlank(month)) {
            content = content.replaceAll("\\$\\{mm}", month);
        }
        if (content.contains("${dd}") && StringUtils.isNoneBlank(day)) {
            content = content.replaceAll("\\$\\{dd}", day);
        }
        SpiderConfig config = processor.getSpiderConfig();
        config.setContent(content);
        processor.setSpiderConfig(config);
        return content;
    }

    private String buildDiscoveryUrl(String discoveryUrl, String year, String month, String day) {
        if (discoveryUrl.contains("${yyyy}")) {
            discoveryUrl = discoveryUrl.replaceAll("\\$\\{yyyy}", year);
        }
        if (discoveryUrl.contains("${mm}")) {
            discoveryUrl = discoveryUrl.replaceAll("\\$\\{mm}", month);
        }
        if (discoveryUrl.contains("${dd}")) {
            discoveryUrl = discoveryUrl.replaceAll("\\$\\{dd}", day);
        }
        return discoveryUrl;
    }

    private LinkedMultiValueMap<String, String> buildParamMap(Map<String, String> paramMap, String year, String month, String day) {
        LinkedMultiValueMap<String, String> newMap = new LinkedMultiValueMap<>();
        paramMap.forEach((key, value) -> {
            if (value.contains("${yyyy}")) {
                value = value.replaceAll("\\$\\{yyyy}", year);
            }
            if (value.contains("${mm}")) {
                value = value.replaceAll("\\$\\{mm}", month);
            }
            newMap.add(key, value);
        });
        return newMap;
    }

    private String fetchIdByDiscovery(String discoveryUrl, String year, String month, String day,
                                      String discoveryType, Site siteRule) {
        if ("defRequest".equals(discoveryType)) {
            discoveryUrl = buildDiscoveryUrl(discoveryUrl, year, month, day);
            String method = siteRule.getRequestMethod();
            Map<String, String> paramMap = siteRule.getRequestParam();
            LinkedMultiValueMap<String, String> newMap = buildParamMap(paramMap, year, month, day);
            Map<String, String> header = siteRule.getRequestHeader();
            HttpHeaders headers = new HttpHeaders();
            String contentType = header.get("content-type");
            if (StringUtils.isNotBlank(contentType)) {
                headers.setContentType(new MediaType(contentType.split("/")[0], contentType.split("/")[1]));
            }
            String host = header.get("Host");
            String origin = header.get("Origin");
            headers.setHost(new InetSocketAddress(host.split(":")[0],
                    host.split(":").length > 1 ? Integer.parseInt(host.split(":")[1]) : 80));
            headers.setOrigin(origin);

            HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(newMap, headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(discoveryUrl, HttpMethod.resolve(method), entity,
                    byte[].class);

            byte[] bodyBytes = response.getBody();
            if (null == bodyBytes) {
                return null;
            }
            String responseStr = new String(bodyBytes);
            if (StringUtils.isBlank(responseStr)) {
                return "";
            }
            String dateIdStr = responseStr.split("!")[1];
            String[] dateIdArr = dateIdStr.split("%");
            Map<String, String> dateIdMap = Maps.newHashMap();
            for (int i = 0; i < dateIdArr.length; i++) {
                String[] singleDateArr = dateIdArr[i].split("-");
                dateIdMap.put(singleDateArr[0], singleDateArr[1]);
            }
            if (dateIdMap.containsKey(day)) {
                String id = dateIdMap.get(day);
                String url = siteRule.getUrl();
                url = url.replaceAll("\\$\\{id}", id);
                return url;
            }
            return "";
        }
        if (!"html".equals(discoveryType)) {
            throw new IllegalArgumentException("html format supported only");
        }
        discoveryUrl = buildDiscoveryUrl(discoveryUrl, year, month, day);

        String responseStr = simplePlainGetRequest(discoveryUrl);
        return fetchUrlFromResponseContext(responseStr, siteRule, year, month, day);
    }

    private String simplePlainGetRequest(String discoveryUrl) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> list = new ArrayList<>();
        headers.setAccept(list);
        ResponseEntity<byte[]> response = restTemplate.exchange(discoveryUrl, HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);

        byte[] bodyBytes = response.getBody();
        if (null == bodyBytes) {
            return null;
        }
        return new String(bodyBytes);
    }

    private String fetchUrlFromResponseContext(String responseStr, Site siteRule,
                                               String year, String month, String day) {
        if (StringUtils.isBlank(responseStr)) {
            return "";
        }
        try {
            HtmlCleaner hc = new HtmlCleaner();
            TagNode tn = hc.clean(responseStr);
            boolean matchById = siteRule.isMatchById();
            String idXpath = siteRule.getHtmlXpathIdRule();
            String aXpath = siteRule.getHtmlXpathARule();
            String dayXpath = siteRule.getHtmlXpathDayRule();
            String matchRule = siteRule.getHtmlXpathMatchRule();
            String matchType = siteRule.getHtmlXpathMatchType();
            String aPrefix = siteRule.getHtmlXpathAPrefix();
            // 只需找到id即进行下一步爬取的情况
            if (matchById) {
                String idStr = Arrays.toString(tn.evaluateXPath(idXpath));

                return idStr;
            }
            Object[] dayArr;
            dayArr = tn.evaluateXPath(dayXpath);
            Object[] aArr;
            aArr = tn.evaluateXPath(aXpath);

            if (null == dayArr || null == aArr) {
                return "";
            }
            for (int i = 0; i < dayArr.length; i++) {
                String judgeText = dayArr[i].toString();
                if (StringUtils.isBlank(matchRule)) {
                    // 目前只支持dd 日期匹配方式 --- 抚州日报
                    if (!"dd".equals(matchType)) {
                        return "";
                    }
                    if (!judgeText.equals(day)) {
                        continue;
                    }
                    return aArr[i].toString();
                }
                String matchText = "";
                Pattern pattern = Pattern.compile(matchRule);
                Matcher matcher = pattern.matcher(judgeText);
                while (matcher.find()) {
                    matchText = matcher.group(1);
                }
                if (!"yyyymmdd".equals(matchType)) {
                    return "";
                }
                String replaceRule = siteRule.getHtmlXpathMatchReplaceRule();
                if (StringUtils.isNoneBlank(replaceRule)) {
                    matchText = matchText.replaceAll(replaceRule, "");
                }
                if (matchText.equals(year + month + day)) {
                    if (StringUtils.isBlank(aPrefix)) {
                        String htmlXpathMatchUrlReplaceRule = siteRule.getHtmlXpathMatchUrlReplaceRule();
                        if (StringUtils.isNotBlank(htmlXpathMatchUrlReplaceRule)) {
                            return aArr[i].toString().replaceAll(htmlXpathMatchUrlReplaceRule, "");
                        }
                        return aArr[i].toString();
                    }
                    String htmlXpathMatchUrlReplaceRule = siteRule.getHtmlXpathMatchUrlReplaceRule();
                    if (StringUtils.isNotBlank(htmlXpathMatchUrlReplaceRule)) {
                        return aPrefix + aArr[i].toString().replaceAll(htmlXpathMatchUrlReplaceRule, "");
                    }
                    return aPrefix + aArr[i].toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String fetchIdByDiscovery(String discoveryUrl, String dateStr) {
        HttpHeaders headers = new HttpHeaders();
        List<MediaType> list = new ArrayList<>();
        headers.setAccept(list);
        ResponseEntity<byte[]> response = restTemplate.exchange(discoveryUrl, HttpMethod.GET, new HttpEntity<byte[]>(headers),
                byte[].class);

        byte[] bodyBytes = response.getBody();
        if (null == bodyBytes) {
            return "";
        }
        String responseStr = new String(bodyBytes);
        ResponseContent<DiscoveryContent> content = JSON.parseObject(responseStr, new TypeReference<ResponseContent<DiscoveryContent>>() {
        });
        List<DiscoveryContent> data = content.getData();
        if (data.isEmpty()) {
            return "";
        }
        String id = "";
        for (DiscoveryContent discovery : data) {
            String pushTime = discovery.getPushTime();
            if (!dateStr.equals(pushTime)) {
                continue;
            }
            return discovery.getId();
        }
        return id;
    }

    private String fetchUrlByDiscovery(String discoveryUrl, String dateStr, String siteUrl) {
        String id = fetchIdByDiscovery(discoveryUrl, dateStr);
        return siteUrl.replace("${id}", id);
    }

}