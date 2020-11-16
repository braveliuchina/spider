package cn.cnki.spider.spider;

import cn.cnki.spider.common.pojo.ArticleDO;
import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.entity.SpiderConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Data
public class AbstractCloudNewspaperProcessor implements PageProcessor {

    private final SpiderConfigDao spiderConfigDao;

    private SpiderConfig spiderConfig;

    private Site site;

    private cn.cnki.spider.entity.Site siteRule;

    private Map<String, Content> spiderRuleMap;

    private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>() {};

    private Map<String, cn.cnki.spider.entity.Page> pageMap;

    private Map<String, String> articleIdMap;

    private String newspaperName;

    private String code;

    // 任务相关
    private Long jobId;

    private String hisId;

    private Long templateId;

    private List<ArticleDO> articles;

    public AbstractCloudNewspaperProcessor(SpiderConfigDao spiderConfigDao) {
        this.spiderConfigDao = spiderConfigDao;
    }

    @Override
    public Site getSite() {

        if (null != site && null != siteRule && null != spiderRuleMap) {
            return site;
        }
        String paperName = this.getNewspaperName();
        String code = this.getCode();
        this.setSpiderConfig(StringUtils.isNoneBlank(code) ?
                spiderConfigDao.getConfigByCode(code) : spiderConfigDao.getConfig(paperName));
        String json = spiderConfig.getSite();
        String contentJson = spiderConfig.getContent();
        siteRule = JSONObject.parseObject(json, cn.cnki.spider.entity.Site.class);
        HashMap<String, Content> content = (HashMap<String, Content>) JSONObject.parseObject(contentJson,
                typeReference);
        spiderRuleMap = content;
        String charset = siteRule.getCharset();
        site = Site.me().setCharset(StringUtils.isBlank(charset) ? "UTF-8" : charset)
                .setRetryTimes(siteRule.getRetryTimes()).setCycleRetryTimes(siteRule.getCycleRetryTimes())
                .setSleepTime(((Long) (siteRule.getSleepTime())).intValue());
        return site;

    }

    public void rebuildRuleMap(String contentJson) {
        if (StringUtils.isBlank(contentJson)) {
            return ;
        }
        HashMap<String, Content> content = (HashMap<String, Content>) JSONObject.parseObject(contentJson,
                typeReference);
        spiderRuleMap = content;
    }

    private String buildArticleImagePrefix(String prefix, String pageUrl,
                                           boolean imgPrefix, boolean imgPrefixUsePrefix) {
        if (!imgPrefix) {
            return "";
        }
        return imgPrefixUsePrefix ? prefix : pageUrl.substring(0, pageUrl.lastIndexOf("/")) + "/";
    }

    private void buildArticleByKey(String key, List<String> ignore, Map<String, Content> ruleMap, ArticleDO article,
                                   Page page, Selectable node, String pageNoIndex) {
        if (null != ignore && !ignore.isEmpty() && ignore.contains(key)) {
            return;
        }
        Content content = ruleMap.get(key);
        String contentType = content.getContentType();
        String selector = content.getSelector();
        String rule = content.getRule();
        boolean multi = content.isMulti();

        String filterRegex = content.getFilterRegex();
        boolean joinAll = content.isAll();
        int index = content.getIndex();
        String pageUrl = page.getUrl().toString();
        boolean imgPrefix = content.isImgPrefix();
        boolean imgPrefixUsePrefix = content.isImgPrefixUsePrefix();
        Content prefixContent = ruleMap.get("prefix");
        String prefix = "";
        if (null != prefixContent) {
            prefix = prefixContent.getRule();
        }
        String imageUrl = buildArticleImagePrefix(prefix, pageUrl, imgPrefix, imgPrefixUsePrefix);
        // xml解析方式 头部 公用属性
        boolean isHead = content.isHead();

        // HTML --- REGEX
        if ("html".equals(contentType) && "regex".equals(selector) && multi) {
            if (content.isHead()) {
                String text = page.getHtml().regex(rule).toString();
                buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
            } else {
                if (null != node) {
                    List<String> valueList = node.regex(rule).all();
                    buildArticle(article, key, valueList, filterRegex, joinAll,
                            index, rule, pageNoIndex, imageUrl, content, page);
                } else {
                    List<String> valueList = page.getHtml().regex(rule).all();
                    buildArticle(article, key, valueList, filterRegex, joinAll,
                            index, rule, pageNoIndex, imageUrl, content, page);
                }
            }

        }
        if ("html".equals(contentType) && "regex".equals(selector) && !multi) {
            if (isHead) {
                String text = page.getHtml().regex(rule).toString();
                buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
            } else {
                if (null != node) {
                    String text = node.regex(rule).toString();
                    buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
                } else {
                    String text = page.getHtml().regex(rule).toString();
                    buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
                }
            }
        }

        // HTML --- XPATH
        if ("html".equals(contentType) && "xpath".equals(selector) && multi) {
            if (isHead) {
                String text = page.getHtml().xpath(rule).toString();
                buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
            } else {
                if (null != node) {
                    List<String> valueList = node.xpath(rule).all();
                    buildArticle(article, key, valueList, filterRegex, joinAll,
                            index, rule, pageNoIndex, imageUrl, content, page);
                } else {
                    List<String> valueList = page.getHtml().xpath(rule).all();
                    buildArticle(article, key, valueList, filterRegex, joinAll,
                            index, rule, pageNoIndex, imageUrl, content, page);
                }
            }
        }

        if ("html".equals(contentType) && "xpath".equals(selector) && !multi) {
            if (isHead) {
                String text = page.getHtml().xpath(rule).toString();
                buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
            } else {
                if (null != node) {
                    String text = node.xpath(rule).toString();
                    buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
                } else {
                    String text = page.getHtml().xpath(rule).toString();
                    buildArticle(article, key, text, rule, pageNoIndex, imageUrl, content);
                }
            }

        }

        // URL --- XPATH
        if ("url".equals(contentType) && "xpath".equals(selector)) {
            buildArticle(article, key, page.getUrl().xpath(rule).toString(), rule, pageNoIndex, imageUrl, content);
        }

        // URL --- REGEX
        if ("url".equals(contentType) && "regex".equals(selector)) {
            buildArticle(article, key, page.getUrl().regex(rule).toString(), rule, pageNoIndex, imageUrl, content);
        }
        // 直接用规则rule赋值
        if ("html".equals(contentType) && "direct".equals(selector)) {
            buildArticle(article, key, "", rule, pageNoIndex, imageUrl, content);
        }
    }

    private List<String> filterCascadeUrl(List<String> urls, Content articleContent) {
        boolean gt = articleContent.isGt();
        if (!gt) {
            return urls;
        }
        String gtRegex = articleContent.getGtRegex();
        String gtId = articleContent.getGtId();
        return urls.stream().map(url -> {
            Pattern pattern = Pattern.compile(gtRegex);
            Matcher matcher = pattern.matcher(url);
            while (matcher.find()) {
                String id = matcher.group(1);
                if (Integer.parseInt(gtId) != Integer.parseInt(id)) {
                    return null;
                }
            }
            return url;
        }).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    private void addCascadeLinks(String contentTypeArticle, String selectorArticle,
                                 String ruleArticle, Page page, String metaPrefix, Content articleContent) {
        if ("html".equals(contentTypeArticle) && "regex".equals(selectorArticle)) {
            List<String> urls = page.getHtml().links().regex(ruleArticle).all();
            urls = filterCascadeUrl(urls, articleContent);
            page.addTargetRequests(urls);
        }
        if ("html".equals(contentTypeArticle) && "xpath".equals(selectorArticle)) {
            page.addTargetRequests(page.getHtml().links().xpath(ruleArticle).all());
        }
        if ("html".equals(contentTypeArticle) && "meta".equals(selectorArticle)) {
            List<String> content = page.getHtml().xpath("meta[@HTTP-EQUIV='REFRESH']/@CONTENT").all();
            List<String> links = Lists.newArrayList();
            if (!content.isEmpty()) {
                String url = page.getUrl() + (content.get(0).split(";")[1]).split("=")[1];
                links.add(url);
            }
            page.addTargetRequests(links);
            page.addTargetRequests(page.getHtml().links().regex(ruleArticle).all());
        }

        if ("html".equals(contentTypeArticle) && "metaPrefix".equals(selectorArticle)) {
            List<String> content = page.getHtml().xpath("meta[@HTTP-EQUIV='REFRESH']/@CONTENT").all();
            List<String> links = Lists.newArrayList();
            if (!content.isEmpty()) {
                String url = metaPrefix + (content.get(0).split(";")[1]).split("=")[1];
                links.add(url);
            }
            page.addTargetRequests(links);
            page.addTargetRequests(page.getHtml().links().regex(ruleArticle).all());
        }
    }

    private String filterNullString(String item) {
        if (StringUtils.isBlank(item)) {
            return "";
        }
        if ("null".equals(item)) {
            return "";
        }
        return item;
    }
    private void buildArticleContent(ArticleDO article, Content contentArticle, Page page) {
        String content = article.getContent();
        String imgs = article.getImage();
        // 只有图片没有内容的情况
        if (StringUtils.isBlank(content) && StringUtils.isNoneBlank(imgs)) {
            content = buildImgContent(content, imgs, article, contentArticle);
        }
        if (!StringUtils.isBlank(content) && !content.contains("</div>") && !content.contains("</p>")) {
            String introTitle = filterNullString(article.getIntroTitle());
            String author = filterNullString(article.getAuthor());
            String title = filterNullString(article.getTitle());
            String subTitle = filterNullString(article.getSubTitle());
            String source = filterNullString(article.getSource());
            String zhuanBan = filterNullString(article.getZhuanBan());
            String property = filterNullString(article.getProperty());
            String contentNew = StringUtils.isBlank(content) ? "" : content.replace("\n", "<br/>");
            content = "<div><span style='font-weight:bold;font-size:22px;'>" + introTitle + "</span></div>" + "<div>"
                    + author + "</div>" + "<div><span style='font-weight:bold;font-size:16px;'>" + title
                    + "</span></div>" + "<div>" + subTitle + "</div>" + "<div>" + source + "</div>" + "<div>" + zhuanBan
                    + "</div>" + "<div>" + property + "</div>" + "<div>" + contentNew + "</div>";
            if (StringUtils.isBlank(imgs)) {
                article.setContentAll(content);
                return;
            }

            content = buildImgContent(content, imgs, article, contentArticle);
        }
        Map<String, String> contentImgReplacer = contentArticle.getContentImgReplacer();
        if (null != contentImgReplacer) {
            for (String key : contentImgReplacer.keySet()) {
                String value = contentImgReplacer.get(key);
                if ("${prefix}".equals(value)) {
                    String prefix = article.getPrefix();
                    try {
                        content = content.replaceAll(key, "$1" + prefix + "$2\"");
                    } catch (IndexOutOfBoundsException e) {
                        content = content.replaceAll(key, prefix);
                    }
                } else if ("${pageUrl}".equals(value)) {
                    String pageUrl = page.getUrl().toString();
                    pageUrl = pageUrl.substring(0, pageUrl.lastIndexOf("/")) + "/";
                    content = content.replaceAll(key, "$1" + pageUrl + "$2\">");
                } else {
                    content = content.replaceAll(key, value);
                }
            }
        }
        String jpgPrefixReplaceRegex = contentArticle.getJpgPrefixReplaceRegex();
        if (StringUtils.isNoneBlank(jpgPrefixReplaceRegex)) {
            String prefix = article.getPrefix();
            if (StringUtils.isNotBlank(prefix)) {
                content = content.replaceAll(jpgPrefixReplaceRegex, prefix);
                article.setContent(content);
            }
        }
        article.setContentAll(content);
    }

    private String buildImgContent(String content, String imgs, ArticleDO article, Content contentArticle) {
        boolean jpgConcatDate = contentArticle.isJpgConcatDate();

        if (StringUtils.isBlank(content)) {
            String title = article.getTitle();
            content = "<div><span style='font-weight:bold;font-size:16px;'>" + title + "</span></div>";
        }
        String[] imgArr = imgs.split(",");
        String date = article.getDate();
        String prefix = article.getPrefix();
        for (int i = 0; i < imgArr.length; i++) {
            if (jpgConcatDate) {
                imgArr[i] = date + "/" + imgArr[i];
            }
            content = content + "<div><img src='" + (StringUtils.isBlank(prefix)? "" : prefix) + imgArr[i] + "'/></div>";
        }
        article.setImage(StringUtils.join(imgArr, ","));
        return content;
    }

    @Override
    public void process(Page page) {

        Map<String, Content> ruleMap = getSpiderRuleMap();
        Content contentArticle = ruleMap.get("article");
        String ruleArticle = contentArticle.getRule();
        String contentTypeArticle = contentArticle.getContentType();
        String selectorArticle = contentArticle.getSelector();
        boolean digest = contentArticle.isDigest();
        boolean isLink = contentArticle.isLinks();
        List<String> ignore = contentArticle.getIgnore();
//        String directory = contentArticle.getDirectory();
        String digestKey = contentArticle.getDigestKey();
        String metaPrefix = contentArticle.getMetaPrefix();
        String pageNoIndex = contentArticle.getPageNoIndex();
        boolean isXml = contentArticle.isXml();

        List<ArticleDO> spiderArticles = this.getArticles();
        if (null != spiderArticles && !spiderArticles.isEmpty()) {
            spiderArticles.forEach(article -> {
                buildArticleContent(article, contentArticle, page);
                article.setTemplateId(templateId);
                article.setJobId(jobId);
                article.setHisId(hisId);
            });
            page.putField("articles", spiderArticles);
//            page.putField("directory", directory);
            return;
        }
        if (isXml) {
            List<Selectable> nodes = null;
            if ("html".equals(contentTypeArticle) && "regex".equals(selectorArticle)) {
                nodes = page.getHtml().regex(ruleArticle).nodes();
            } else if ("html".equals(contentTypeArticle) && "xpath".equals(selectorArticle)) {
                nodes = page.getHtml().xpath(ruleArticle).nodes();
            }
            List<ArticleDO> articles = Lists.newArrayList();
            if (null == nodes) {
                return;
            }
            for (Selectable node : nodes) {
                ArticleDO article = buildArticle(digestKey, ruleMap,
                        ignore, page, contentArticle, node, page, pageNoIndex);
                if (null != article) {
                    article.setTemplateId(templateId);
                    article.setJobId(jobId);
                    article.setHisId(hisId);
                    articles.add(article);
                }
            }
            page.putField("articles", articles);
//            page.putField("directory", directory);
            return;
        }
        // 是否持续抓取
        if (isLink) {
            addCascadeLinks(contentTypeArticle, selectorArticle, ruleArticle, page, metaPrefix, contentArticle);
        }
        // html模式下 是否要拿到一对多摘要信息
        if (digest) {
            boolean needNextCrawl = false;
            // 文章版面信息抓取
            try {
                needNextCrawl = doDigestCrawl(page, ruleMap);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                log.warn("文章版面信息抓取失败", e);
            }
            if (!needNextCrawl) {
                return;
            }
        }
        String detailRegex = contentArticle.getDigestDetailPageUrlRegex();
        if (StringUtils.isNotBlank(detailRegex) && !page.getUrl().toString().matches(detailRegex)) {
            return;
        }
        // 文章详情页信息抓取
        ArticleDO article = buildArticle(digestKey, ruleMap, ignore, page, contentArticle, null, page, pageNoIndex);
        if (null == article) {
            return;
        }
        article.setTemplateId(templateId);
        article.setJobId(jobId);
        article.setHisId(hisId);
        // 将变量塞入上下文
        page.putField("article", article);
//        page.putField("directory", directory);
    }

    private ArticleDO buildArticle(String digestKey, Map<String, Content> ruleMap, List<String> ignore, Page page,
                                       Content contentArticle, Selectable node, Page pageNew, String pageNoIndex) {
        long millis = System.currentTimeMillis();
        ArticleDO article = new ArticleDO();
        article.setTemplateId(templateId);
        article.setCtime(millis);
        article.setUtime(millis);
        for (String key : ruleMap.keySet()) {
            buildArticleByKey(key, ignore, ruleMap, article, page, node, pageNoIndex);
        }
        // 判断是否为实体文章
        if (StringUtils.isBlank(article.getTitle())) {
            return null;
        }
        // 替换页面内容中的 变量符号
        buildArticleContent(article, contentArticle, pageNew);
        // 日期, 版号, 版名, jpg, pdf等 版面信息
        buildDigestInfo(digestKey, article);
        return article;
    }

    protected void buildDigestInfo(String digestKey, ArticleDO article) {
        if (!"cfgId".equals(digestKey) && !"pageNo".equals(digestKey)) {
            return;
        }
        String pageId = "";
        if ("cfgId".equals(digestKey)) {
            String cfgId = article.getCfgId();
            Map<String, String> articleIdMap = this.getArticleIdMap();
            if (null == articleIdMap || !articleIdMap.containsKey(cfgId)) {
                return;
            }
            pageId = articleIdMap.get(cfgId);
        }
        if ("pageNo".equals(digestKey)) {
            pageId = article.getPageNo();
        }
        Map<String, cn.cnki.spider.entity.Page> pageInfoMap = this.getPageMap();
        if (null == pageInfoMap || StringUtils.isBlank(pageId) || !pageInfoMap.containsKey(pageId)) {
            return;
        }
        cn.cnki.spider.entity.Page pageInfo = pageInfoMap.get(pageId);
        if (null == pageInfo) {
            return;
        }
        String date = pageInfo.getDate();
        String pageNo = pageInfo.getPageNo();
        String pageName = pageInfo.getPageName();
        String jpg = pageInfo.getJpg();
        String pdf = pageInfo.getPdf();
        if (StringUtils.isNotBlank(date)) {
            article.setDate(date);
        }
        if (StringUtils.isNotBlank(pageName)) {
            article.setPageName(pageName);
        }
        if (StringUtils.isNotBlank(pageNo)) {
            article.setPageNo(pageNo);
        }
        if (StringUtils.isNotBlank(jpg)) {
            article.setJpg(jpg);
        }
        if (StringUtils.isNotBlank(pdf)) {
            article.setPdf(pdf);
        }
    }

    protected boolean doDigestCrawl(Page page, Map<String, Content> ruleMap) throws InterruptedException {
        String url = page.getUrl().toString();
        if (StringUtils.isBlank(url)) {
            return false;
        }
        Content contentArticle = ruleMap.get("article");
        String digestDetailPageUrlRegex = contentArticle.getDigestDetailPageUrlRegex();
        Long detailPageSleepMillis = contentArticle.getDetailPageCrawlThreadSleepMillis();
        // 详情页爬虫睡60s 确保文章列表执行完成
        if (url.matches(digestDetailPageUrlRegex)) {
            Thread.sleep(detailPageSleepMillis);
            return true;
        }
        String datePageUrlRegex = contentArticle.getDigestDatePageUrlRegex();
        // http://epaper.scjjrb.com/Media/scjjrb/2020-09-02
        // 设置全局 datePageMap datePageMap
        if (!StringUtils.isBlank(datePageUrlRegex) && url.matches(datePageUrlRegex)) {
            String digestDateRegex = contentArticle.getDigestDateRegex();
            String pageNoPageNameSplitChar = contentArticle.getDigestPageNoPageNameSplitChar();
            buildDatePageMap(datePageUrlRegex, digestDateRegex, pageNoPageNameSplitChar, url, ruleMap, page);
            return false;
        }
        // http://epaper.scjjrb.com/Page/index/pageid/696998.html#696998
        // 设置全局 pageNoArticleListMap
        String articleListRegex = contentArticle.getDigestListPageUrlRegex();
        String digestDetailPageUrlRegexNew = contentArticle.getDigestDetailPageUrlRegex();
        if (!StringUtils.isBlank(articleListRegex) &&
                !StringUtils.isBlank(articleListRegex) && url.matches(articleListRegex)) {
            buildPageIdArticleListMap(articleListRegex, digestDetailPageUrlRegexNew, url, contentArticle, ruleMap,
                    page);
            return false;
        }
        return true;
    }

    private void buildPageIdArticleListMapForPageNo(String regex, String newJpg, Map<String, Content> ruleMap, Page page) {
        Content pageIdContent = ruleMap.get("pageId");
        String pageIdRule = pageIdContent.getRule();
        List<String> pathes = page.getHtml().xpath(pageIdRule).all();

        Content pageNameContent = ruleMap.get("pageName");
        String pageNameRule = pageNameContent.getRule();
        List<String> pageNames = page.getHtml().xpath(pageNameRule).all();

        pageNames.forEach(pageName -> {
            String[] pageArr = pageName.split(" ");
            pageArr[0] = pageArr[0].replace("(", "").replace(")", "").trim();
            pageArr[1] = pageArr[1].trim();
            Map<String, cn.cnki.spider.entity.Page> pageMap = this.getPageMap();
            if (null == pageMap) {
                pageMap = new HashMap<String, cn.cnki.spider.entity.Page>();
                this.setPageMap(pageMap);
            }
            if (!pageMap.containsKey(pageArr[0])) {
                cn.cnki.spider.entity.Page pageInfo = new cn.cnki.spider.entity.Page();
                pageInfo.setPageName(pageArr[1]);
                pageMap.put(pageArr[0], pageInfo);
                return;
            }
            cn.cnki.spider.entity.Page pageInfo = pageMap.get(pageArr[0]);
            if (StringUtils.isBlank(pageInfo.getPageName())) {
                pageInfo.setPageName(pageArr[1]);
                pageMap.put(pageArr[0], pageInfo);
            }
        });

        if (pathes.isEmpty()) {
            return;
        }

        final String regexNew = regex;
        pathes.forEach(path -> {
            if (!path.matches(regexNew)) {
                return;
            }
            Pattern pattern = Pattern.compile(".*>(.*)</a>");
            Matcher matcher = pattern.matcher(path);
            String pageInfoNew = "";
            while (matcher.find()) {
                pageInfoNew = matcher.group(1);
            }
            String[] pageArr = pageInfoNew.split(" ");
            pageArr[0] = pageArr[0].replace("(", "").replace(")", "").trim();
            pageArr[1] = pageArr[1].trim();
            Map<String, cn.cnki.spider.entity.Page> pageMap = this.getPageMap();
            if (!pageMap.containsKey(pageArr[0])) {
                cn.cnki.spider.entity.Page pageInfo = new cn.cnki.spider.entity.Page();
                pageInfo.setJpg(newJpg);
                pageMap.put(pageArr[0], pageInfo);
                return;
            }
            cn.cnki.spider.entity.Page pageInfo = pageMap.get(pageArr[0]);
            if (StringUtils.isBlank(pageInfo.getJpg())) {
                pageInfo.setJpg(newJpg);
                pageMap.put(pageArr[0], pageInfo);
            }
        });
    }

    private void buildPageIdArticleListMap(String regex, String digestDetailPageUrlRegex, String url,
                                           Content articleContent, Map<String, Content> ruleMap, Page page) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);
        String pageId = "";
        while (m.find()) {
            pageId = m.group(1);
        }
        final String newPageId = pageId;
        Content articleIdContent = ruleMap.get("articleId");
        String articleIdRule = articleIdContent.getRule();
        List<String> articleIdList = page.getHtml().xpath(articleIdRule).all();

        // 版面jpg
        Content jpgContent = ruleMap.get("jpg");
        String jpg = "";
        if (null != jpgContent) {
            String jpgRule = jpgContent.getRule();
            jpg = page.getHtml().xpath(jpgRule).toString();
            String jpgRegex = articleContent.getJpgPrefixReplaceRegex();

            Content prefixContent = ruleMap.get("prefix");
            String prefix = prefixContent.getRule();
            if (StringUtils.isNotBlank(jpg) && StringUtils.isNotBlank(jpgRegex)) {
                jpg = jpg.replaceAll(jpgRegex, prefix);
            }
        }
        final String newJpg = jpg;
        // 版面pdf
        Content pdfContent = ruleMap.get("pdf");
        String pdf = "";
        if (null != pdfContent) {
            String pdfRule = pdfContent.getRule();
            pdf = page.getHtml().xpath(pdfRule).toString();
            String pdfRegex = articleContent.getPdfPrefixReplaceRegex();
            boolean usePrefix = pdfContent.isPdfPrefixUsePrefix();
            if (!usePrefix) {
                String prefix = pdfContent.getRule();
                if (StringUtils.isNotBlank(pdf) && StringUtils.isNotBlank(pdfRegex)) {
                    pdf = pdf.replaceAll(pdfRegex, prefix);
                }
            } else {
                Content prefixContent = ruleMap.get("prefix");
                String prefix = prefixContent.getRule();
                if (StringUtils.isNotBlank(pdf) && StringUtils.isNotBlank(pdfRegex)) {
                    pdf = pdf.replaceAll(pdfRegex, prefix);
                }
            }

        }
        // 版号 + 版号名称
        Content pageNoContent = ruleMap.get("pageNo");
        Content pageIdContent = ruleMap.get("pageId");
        String pageNo = "";
        String pageName = "";
        if (null != pageNoContent && pageNoContent.isPageNoArticleMapNeedMatch()) {
            String pageNoRule = pageNoContent.getRule();
            String matchText = page.getHtml().xpath(pageNoRule).toString();
            if (StringUtils.isNoneBlank(matchText)) {
                pageNo = matchText.replaceAll("(\\d\\d)[\u4E00-\u9FA5][\u4E00-\u9FA5]", "$1");
                pageName = matchText.replaceAll("^(\\d+)([\u4E00-\u9FA5])", "$2");
            }
        }
        if (null != pageNoContent && pageNoContent.isPageNoArticleMapNeedMatchAll()) {
            boolean pageNoScript = pageNoContent.isPageNoScriptMatch();
            List<String> pageNoList = Lists.newArrayList();
            List<String> pageIdList = Lists.newArrayList();
            if (!pageNoScript) {
                String pageNoRule = pageNoContent.getRule();
                pageNoList = page.getHtml().xpath(pageNoRule).all();
                pageNoList = pageNoList.stream().filter(StringUtils::isNoneBlank).collect(Collectors.toList());
                String pageIdRule = pageIdContent.getRule();
                pageIdList = page.getHtml().xpath(pageIdRule).all();
                if (pageIdList.isEmpty() || pageNoList.isEmpty()) {
                    return;
                }
                pageIdList = pageIdList.stream()
                        .filter(item -> StringUtils.isNotBlank(item) && item.matches(regex)).collect(Collectors.toList());
                for (int i = 0; i < pageIdList.size(); i++) {
                    String item = pageIdList.get(i);
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(item);
                    String newId = "";
                    while (matcher.find()) {
                        newId = matcher.group(1);
                    }
                    cn.cnki.spider.entity.Page pageInfo = new cn.cnki.spider.entity.Page();
                    String pageInfoStr = pageNoList.get(i);
                    String[] pageArr = pageInfoStr.split(articleContent.getDigestPageNoPageNameSplitChar());
                    String pageNoNew = buildPageNoDirect((pageArr[0]).trim());
                    if (StringUtils.isNotBlank(pageNoNew)) {
                        pageInfo.setPageNo(buildPageNo(pageNoNew));
                    }
                    if (StringUtils.isNotBlank(pageArr[1])) {
                        pageInfo.setPageName(pageArr[1]);
                    }
                    if (null != pageMap && pageMap.containsKey(newId)) {
                        cn.cnki.spider.entity.Page newPage = pageMap.get(newId);
                        newPage.setPageNo(buildPageNo(pageInfo.getPageNo()));
                        newPage.setPageName(pageInfo.getPageName());
                        pageMap.put(newId, newPage);
                    } else if(null != pageMap) {
                        pageMap.put(newId, pageInfo);
                    } else {
                        pageMap = new HashMap<>();
                        pageMap.put(newId, pageInfo);
                    }
                }
            } else {
                String scriptPageNoRegex = pageNoContent.getPageNoScriptMatchRegex();
                int scriptPageNoIndex = pageNoContent.getPageNoScriptMatchRegexIndex();
                String script = page.getRawText();
                Pattern pattern = Pattern.compile(scriptPageNoRegex);
                Matcher matcher = pattern.matcher(script);
                String pageNoJSONString = "";
                while (matcher.find()) {
                    pageNoJSONString = matcher.group(scriptPageNoIndex);
                }
                JSONArray jsonArr = JSON.parseArray(pageNoJSONString);
                pageNoList = jsonArr.stream().map(json -> (String)((JSONObject)json).get("lmmc")).collect(Collectors.toList());
                List<String> banUrls = Lists.newArrayList();
                for (int i = 0; i < pageNoList.size(); i++) {
                    cn.cnki.spider.entity.Page pageInfo = new cn.cnki.spider.entity.Page();
                    String pageInfoStr = pageNoList.get(i);
                    String[] pageArr = pageInfoStr.split(articleContent.getDigestPageNoPageNameSplitChar());
                    String pageNoNew = buildPageNoDirect((pageArr[0]).trim());
                    if (StringUtils.isNotBlank(pageNoNew)) {
                        pageInfo.setPageNo(buildPageNo(pageNoNew));
                    }
                    if (StringUtils.isNotBlank(pageArr[1])) {
                        pageInfo.setPageName(pageArr[1]);
                    }
                    String key = String.valueOf(i + 1);
                    if (null != pageMap && pageMap.containsKey(key)) {
                        cn.cnki.spider.entity.Page newPage = pageMap.get(key);
                        newPage.setPageNo(buildPageNo(pageInfo.getPageNo()));
                        newPage.setPageName(pageInfo.getPageName());
                        pageMap.put(key, newPage);
                    } else if(null != pageMap) {
                        pageMap.put(key, pageInfo);
                    } else {
                        pageMap = new HashMap<>();
                        pageMap.put(key, pageInfo);
                    }
                    if (i + 1 > Integer.parseInt(newPageId)) {
                        String urlNew  = page.getUrl().toString();
                        String prefixUrl1 = urlNew.substring(0, urlNew.lastIndexOf("/"));
                        String prefixUrl2 = prefixUrl1.substring(0, prefixUrl1.lastIndexOf("/") + 1);
                        banUrls.add(prefixUrl2 + key + "/index.htm");
                    }

                }
                page.addTargetRequests(banUrls);
                // 前端渲染文章列表为空的情况
                if (articleIdList.isEmpty()) {
                    String articleIdRegex = articleIdContent.getArticleIdScriptMatchRegex();
                    int articleIdScriptMatchRegexIndex = articleIdContent.getArticleIdScriptMatchRegexIndex();
                    Pattern pattern2 = Pattern.compile(articleIdRegex);
                    Matcher matcher2 = pattern2.matcher(script);
                    String articleIdJSONString = "";
                    while (matcher2.find()) {
                        articleIdJSONString = matcher2.group(articleIdScriptMatchRegexIndex);
                    }
                    JSONArray jsonArr2 = JSON.parseArray(articleIdJSONString);
                    articleIdList = jsonArr2.stream()
                            .map(json -> (String)((JSONObject)json).get("infoid")).collect(Collectors.toList());
                    List<String> cascadeUrls = Lists.newArrayList();
                    String prerfixUrl = page.getUrl().toString().substring(0, page.getUrl().toString().lastIndexOf("/") + 1);
                    for (String articleId: articleIdList) {
                        cascadeUrls.add(prerfixUrl + articleId + ".htm");
                    }
                    page.addTargetRequests(cascadeUrls);
                }
            }
        }

        String pageMapType = articleContent.getPageIdPageListMapType();
        if ("pageNo".equals(pageMapType)) {

            buildPageIdArticleListMapForPageNo(regex, newJpg, ruleMap, page);
            return;
        }

        Map<String, cn.cnki.spider.entity.Page> pageInfoMap = this.getPageMap();

        if (null != pageInfoMap && pageInfoMap.containsKey(newPageId)) {
            cn.cnki.spider.entity.Page newPage = pageInfoMap.get(newPageId);
            if (StringUtils.isNotBlank(jpg)) {
                newPage.setJpg(jpg);
            }
            if (StringUtils.isNotBlank(pdf)) {
                newPage.setPdf(pdf);
            }
            if (StringUtils.isNotBlank(pageNo)) {
                newPage.setPageNo(pageNo);
            }
            if (StringUtils.isNotBlank(pageName)) {
                newPage.setPageName(pageName);
            }
            pageInfoMap.put(newPageId, newPage);
        }
        if (null != pageInfoMap && !pageInfoMap.containsKey(newPageId)) {
            cn.cnki.spider.entity.Page newPage = new cn.cnki.spider.entity.Page();
            if (StringUtils.isNotBlank(jpg)) {
                newPage.setJpg(jpg);
            }
            if (StringUtils.isNotBlank(pdf)) {
                newPage.setPdf(pdf);
            }
            if (StringUtils.isNotBlank(pageNo)) {
                newPage.setPageNo(pageNo);
            }
            if (StringUtils.isNotBlank(pageName)) {
                newPage.setPageName(pageName);
            }
            pageInfoMap.put(newPageId, newPage);
        }
        if (null == pageInfoMap) {
            cn.cnki.spider.entity.Page newPage = new cn.cnki.spider.entity.Page();
            if (StringUtils.isNotBlank(jpg)) {
                newPage.setJpg(jpg);
            }
            if (StringUtils.isNotBlank(pdf)) {
                newPage.setPdf(pdf);
            }
            if (StringUtils.isNotBlank(pageNo)) {
                newPage.setPageNo(pageNo);
            }
            if (StringUtils.isNotBlank(pageName)) {
                newPage.setPageName(pageName);
            }
            pageInfoMap = new HashMap<>();
            pageInfoMap.put(newPageId, newPage);
            this.setPageMap(pageInfoMap);
        }

        if (articleIdList.isEmpty()) {
            return;
        }

        Map<String, String> articleIdMap = this.getArticleIdMap();
        if (null == articleIdMap) {
            articleIdMap = new HashMap<>(articleIdList.size());
        }
        for (String id : articleIdList) {
            Pattern pattern = Pattern.compile(digestDetailPageUrlRegex);
            Matcher matcher = pattern.matcher(id);
            String newId = "";
            while (matcher.find()) {
                newId = matcher.group(1);
            }
            if (StringUtils.isBlank(newId)) {
                newId = id;
            }
            articleIdMap.put(newId, newPageId);
        }
        this.setArticleIdMap(articleIdMap);
    }

    private void buildDatePageMap(String dateRegex, String dateReplaceRegex, String digestPageNoPageNameSplitChar,
                                  String url, Map<String, Content> ruleMap, Page page) {
        Pattern p = Pattern.compile(dateRegex);
        Matcher m = p.matcher(url);
        String date = "";
        while (m.find()) {
            if (StringUtils.isNotBlank(dateReplaceRegex)) {
                date = m.group(1).replace(dateReplaceRegex, "");
                break;
            }
            date = m.group(1);
        }

        List<String> pdfList = Lists.emptyList();
        List<String> pageIdList = Lists.emptyList();
        List<String> pageNoList = Lists.emptyList();

        Content pageIdContent = ruleMap.get("pageId");
        String pageIdRule = pageIdContent.getRule();
        pageIdList = page.getHtml().xpath(pageIdRule).all();

        Content pageNoContent = ruleMap.get("pageNo");
        String pageNoRule = pageNoContent.getRule();
        pageNoList = page.getHtml().xpath(pageNoRule).all();

        if (ruleMap.containsKey("pdf")) {
            Content pdfContent = ruleMap.get("pdf");
            String pdfRule = pdfContent.getRule();
            pdfList = page.getHtml().xpath(pdfRule).all();
        }

        if (pageNoList.size() != pageIdList.size() || pageNoList.isEmpty()) {
            return;
        }

        Map<String, cn.cnki.spider.entity.Page> pageMap = new HashMap<>();
        for (int i = 0; i < pageIdList.size(); i++) {
            cn.cnki.spider.entity.Page pageInfo = new cn.cnki.spider.entity.Page();
            if (StringUtils.isNotBlank(date)) {
                pageInfo.setDate(date);
            }
            String pageInfoStr = pageNoList.get(i);
            String[] pageArr = pageInfoStr.split(digestPageNoPageNameSplitChar);
            String pageNoNew = buildPageNoDirect((pageArr[0]).trim());
            if (StringUtils.isNotBlank(pageNoNew)) {
                pageInfo.setPageNo(pageNoNew);
            }
            if (StringUtils.isNotBlank(pageArr[1])) {
                pageInfo.setPageName(pageArr[1]);
            }
            if (StringUtils.isNotBlank(pdfList.get(i))) {
                pageInfo.setPdf(pdfList.get(i));
            }
            pageMap.put(pageIdList.get(i), pageInfo);
        }
        this.setPageMap(pageMap);
    }

    protected void buildArticle(ArticleDO article, String key, List<String> valueList, String filterRegex,
                                boolean joinAll, int index, String selector,
                                String pageNoIndex, String imgPrefix, Content content, Page page) {
        if (null == valueList || valueList.isEmpty()) {
            return;
        }
        List<String> result = Lists.newArrayList();
        for (int i = 0; i < valueList.size(); i++) {
            String s = valueList.get(i);
            String resultStr = filterBlankSpecialChar(s);
            if (StringUtils.isNoneBlank(filterRegex)) {
                resultStr = resultStr.replaceAll(filterRegex, "");
            }
            if (StringUtils.isNoneBlank(imgPrefix)) {
                resultStr = imgPrefix + resultStr;
            }
            result.add(resultStr);
        }
        result = result.stream().filter(StringUtils::isNoneBlank).collect(Collectors.toList());

        String actualValue = "";
        if (joinAll) {
            actualValue = StringUtils.join(result, ",");
            buildArticle(article, key, actualValue, selector, pageNoIndex, imgPrefix, content);
            return;
        }
        if (index < valueList.size()) {
            actualValue = valueList.get(index);
            buildArticle(article, key, actualValue, selector, pageNoIndex, imgPrefix, content);
            return;
        }

        buildArticle(article, key, valueList.get(valueList.size() - 1), selector, pageNoIndex, imgPrefix, content);

    }

    protected void buildArticle(ArticleDO article, String key, String value, String rule,
                                String pageNoIndex, String imgPrefix, Content content) {
        value = filterBlankSpecialChar(value);
        if ("date".equals(key)) {
            article.setDate(buildDate(value));
        }
        if ("time".equals(key)) {
            article.setTime(value);
        }
        if ("cfgId".equals(key)) {
            article.setCfgId(value);
        }
        if ("pageNo".equals(key)) {
            if (value.contains("：")) {
                if ("0".equals(pageNoIndex)) {
                    article.setPageNo(buildPageNo(value.split("：")[0].trim()));
                    return;
                }
                article.setPageNo(buildPageNo(value.split("：")[1].trim()));
                return;
            }
            if (value.contains(":")) {
                article.setPageNo(buildPageNo(value.split(":")[0].trim()));
                return;
            }
            if (value.contains(".")) {
                value = value.trim().split("\\.")[0];
            }
            String matchValue = content.getMatchValue();
            if (StringUtils.isNoneBlank(matchValue)) {
                value = String.format(matchValue, value);
                article.setPageNo(value);
            }
            if (!value.contains(",")) {
                article.setPageNo(buildPageNo(value));
                return;
            }
            article.setPageNo(buildPageNo(value.split(",")));
        }
        if ("pageName".equals(key)) {
            if (value.contains("：")) {
                if ("0".equals(pageNoIndex)) {
                    article.setPageName(value.split("：")[1].trim());
                    return;
                }
            }
            if (value.contains(":")) {
                article.setPageName(value.split(":")[1].trim());
                return;
            }
            if (value.contains(".")) {
                value = value.trim().split("\\.")[1];
                article.setPageName(value);
            }
            article.setPageName(value);
        }
        if ("introTitle".equals(key)) {
            article.setIntroTitle(value);
        }
        if ("title".equals(key)) {
            article.setTitle(value);
        }
        if ("subTitle".equals(key)) {
            article.setSubTitle(value);
        }
        if ("property".equals(key)) {
            article.setProperty(value);
        }
        if ("zhuanBan".equals(key)) {
            article.setZhuanBan(value);
        }
        if ("author".equals(key)) {
            article.setAuthor(value);
        }
        if ("source".equals(key)) {
            article.setSource(value);
        }
        if ("content".equals(key)) {
            article.setContent(value);
        }
        if ("image".equals(key)) {
            String img = doDirectoryFilter(value);
            if (StringUtils.isNotBlank(img) && !img.startsWith("http")) {
                img = imgPrefix + img;
            }
            article.setImage(img);
        }
        if ("prefix".equals(key)) {
            article.setPrefix(rule);
        }
    }

    protected String doDirectoryFilter(String path) {
        return path.replaceAll("\\.\\./", "");
    }

    private static String buildPageNoDirect(String item) {
        if ("第一版".equals(item)) {
            return "01";
        }
        if ("第二版".equals(item)) {
            return "02";
        }
        if ("第三版".equals(item)) {
            return "03";
        }
        if ("第四版".equals(item)) {
            return "04";
        }
        if ("第五版".equals(item)) {
            return "05";
        }
        if ("第六版".equals(item)) {
            return "06";
        }
        if ("第七版".equals(item)) {
            return "07";
        }
        if ("第八版".equals(item)) {
            return "08";
        }
        if ("第九版".equals(item)) {
            return "09";
        }
        if ("第十版".equals(item)) {
            return "10";
        }
        if ("第十一版".equals(item)) {
            return "11";
        }
        if ("第十二版".equals(item)) {
            return "12";
        }
        if ("第十三版".equals(item)) {
            return "13";
        }
        if ("第十四版".equals(item)) {
            return "14";
        }
        if ("第十五版".equals(item)) {
            return "15";
        }
        if ("第十六版".equals(item)) {
            return "16";
        }
        if ("第十七版".equals(item)) {
            return "17";
        }
        if ("第十八版".equals(item)) {
            return "18";
        }
        if ("第十九版".equals(item)) {
            return "19";
        }
        if ("第二十版".equals(item)) {
            return "20";
        }
        if ("第二十一版".equals(item)) {
            return "21";
        }
        if ("第二十二版".equals(item)) {
            return "22";
        }
        if ("第二十三版".equals(item)) {
            return "23";
        }
        if ("第二十四版".equals(item)) {
            return "24";
        }
        return item;
    }

    private String buildPageNo(String... args) {
        if (args.length < 2) {
            return "";
        }
        String secondName = args[1];
        if (args.length == 3) {
            if ("第一版".equals(secondName)) {
                return "02";
            }
            if ("第二版".equals(secondName)) {
                return "03";
            }
            if ("第三版".equals(secondName)) {
                return "04";
            }
            if ("第四版".equals(secondName)) {
                return "05";
            }
            if ("第五版".equals(secondName)) {
                return "06";
            }
            if ("第六版".equals(secondName)) {
                return "07";
            }
            if ("第七版".equals(secondName)) {
                return "08";
            }
            if ("第八版".equals(secondName)) {
                return "09";
            }
            if ("第九版".equals(secondName)) {
                return "10";
            }
            if ("第十版".equals(secondName)) {
                return "11";
            }
            if ("第十一版".equals(secondName)) {
                return "12";
            }
        }
        if ("第二版".equals(secondName)) {
            return "01";
        }
        if ("第三版".equals(secondName)) {
            return "04";
        }
        if ("第四版".equals(secondName)) {
            return "05";
        }
        if ("第五版".equals(secondName)) {
            return "06";
        }
        if ("第六版".equals(secondName)) {
            return "07";
        }
        if ("第七版".equals(secondName)) {
            return "08";
        }
        if ("第八版".equals(secondName)) {
            return "09";
        }
        if ("第九版".equals(secondName)) {
            return "10";
        }
        if ("第十版".equals(secondName)) {
            return "11";
        }
        if ("第十一版".equals(secondName)) {
            return "12";
        }
        if ("第十二版".equals(secondName)) {
            return "13";
        }
        String firstName = args[0];
        if ("第一版".equals(firstName)) {
            return "02";
        }
        if ("第二版".equals(firstName)) {
            return "03";
        }
        if ("第三版".equals(firstName)) {
            return "04";
        }
        if ("第四版".equals(firstName)) {
            return "05";
        }
        if ("第五版".equals(firstName)) {
            return "06";
        }
        if ("第六版".equals(firstName)) {
            return "07";
        }
        if ("第七版".equals(firstName)) {
            return "08";
        }
        if ("第八版".equals(firstName)) {
            return "09";
        }
        if ("第九版".equals(secondName)) {
            return "10";
        }
        if ("第十版".equals(secondName)) {
            return "11";
        }
        if ("第十一版".equals(secondName)) {
            return "12";
        }
        return "";
    }

    private String buildPageNo(String pageNo) {
        if (StringUtils.isBlank(pageNo)) {
            return pageNo;
        }
        if (StringUtils.isBlank(pageNo.trim())) {
            return pageNo;
        }
        if (pageNo.contains("第") || pageNo.contains("版")) {
            return buildPageNo(pageNo.replaceAll("第", "").replaceAll("版", ""));
        }
        if (pageNo.trim().matches("[\u4E00-\u9FA5]+")) {
            return pageNo;
        }
        if (pageNo.length() < 2) {
            return "0" + pageNo;
        }
        return pageNo;
    }

    private String buildDate(String date) {
        if (date.contains("/")) {
            String[] dateArr = date.split("/");
            if (dateArr.length != 3) {
                return buildDate(date.replaceAll("/", ""));
            }
            String month = dateArr[1];
            if (month.length() < 2) {
                dateArr[1] = "0" + month;
            }
            String day = dateArr[2];
            if (day.length() < 2) {
                dateArr[2] = "0" + day;
            }
            return StringUtils.join(dateArr, "");
        }
        if (date.contains("年") || date.contains("月") || date.contains("日")) {
            date = date.replaceAll("[年月日]", "");
        }
        if (date.contains("本期出版期：")) {
            date = date.replaceAll("本期出版期：", "");
        }
        if (date.contains("发布时间：")) {
            date = date.replaceAll("发布时间：", "");
        }
        if (date.contains("-")) {
            return buildDate(date.replaceAll("-", ""));
        }
        if (date.contains("　")) {
            return date.split("　")[0].trim();
        }
        if (date.contains(" ")) {
            return date.split(" ")[0].trim();
        }
        return date;
    }

    private String filterBlankSpecialChar(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return "";
        }
        if (!candidate.contains("CDATA")) {
            return candidate.trim();
        }
        Pattern p = Pattern.compile(".*\\[CDATA\\[(.*)]].*");
        Matcher m = p.matcher(candidate);
        if (m.matches()) {
            return m.group(1).trim();
        }
        return "";
    }
}