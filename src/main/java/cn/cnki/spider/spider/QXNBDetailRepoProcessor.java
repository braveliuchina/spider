package cn.cnki.spider.spider;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.entity.SpiderConfig;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Data
//@Component
public class QXNBDetailRepoProcessor implements PageProcessor {

	private final SpiderConfigDao spiderConfigDao;

	private SpiderConfig spiderConfig;

	private Site site;

	private cn.cnki.spider.entity.Site siteRule;

	private Map<String, Content> spiderRuleMap;

	private Map<String, cn.cnki.spider.entity.Page> pageMap;

	private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>() {
	};

	public QXNBDetailRepoProcessor(SpiderConfigDao spiderConfigDao) {
		this.spiderConfigDao = spiderConfigDao;
	}

	@Override
	public Site getSite() {

		if (null != site && null != siteRule && null != spiderRuleMap) {
			return site;
		}
		this.setSpiderConfig(spiderConfigDao.getConfig("黔西南日报"));
		String json = spiderConfig.getSite();
		String contentJson = spiderConfig.getContent();
		siteRule = JSONObject.parseObject(json, cn.cnki.spider.entity.Site.class);
		HashMap<String, Content> content = (HashMap<String, Content>) JSONObject.parseObject(contentJson,
				typeReference);
		spiderRuleMap = content;
		site = Site.me().setRetryTimes(siteRule.getRetryTimes()).setCycleRetryTimes(3)
				.setSleepTime(((Long) (siteRule.getSleepTime())).intValue());
		return site;

	}

	@Override
	public void process(Page page) {

		Map<String, Content> ruleMap = getSpiderRuleMap();
		Content contentArticle = ruleMap.get("article");
		String ruleArticle = contentArticle.getRule();
		String contentTypeArticle = contentArticle.getContentType();
		String selectorArticle = contentArticle.getSelector();
		boolean isLink = contentArticle.isLinks();
		if (isLink && "html".equals(contentTypeArticle) && "regex".equals(selectorArticle)) {
			List<String> links = page.getHtml().links().regex(ruleArticle).all();
			if (links.isEmpty()) {
				List<String> content = page.getHtml().xpath("meta[@HTTP-EQUIV='REFRESH']/@CONTENT").all();
				if (!content.isEmpty()) {
					String url = page.getUrl() + (content.get(0).split(";")[1]).split("=")[1];
					links.add(url);
				}
			}
			page.addTargetRequests(links);
		} else if (isLink && "html".equals(contentTypeArticle) && "xpath".equals(selectorArticle)) {
			page.addTargetRequests(page.getHtml().links().xpath(ruleArticle).all());
		}
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
		// 文章详情页信息抓取
		long millis = System.currentTimeMillis();
		SpiderArticle article = SpiderArticle.builder().build();
		article.setProtocalId(spiderConfig.getId());
		article.setCtime(millis);
		article.setUtime(millis);
		for (String key : ruleMap.keySet()) {
			Content content = ruleMap.get(key);
			String contentType = content.getContentType();
			String selector = content.getSelector();
			String rule = content.getRule();
			if ("article".equals(key) || "pageId".equals(key) || "pageName".equals(key) || "articleId".equals(key)) {
				continue;
			}
			if ("html".equals(contentType) && "regex".equals(selector)) {
				if ("image".equals(key)) {
					List<String> text = page.getHtml().regex(rule).all();
					List<String> result = text.stream().map(s -> s.replaceAll("../../", ""))
							.collect(Collectors.toList());
					buildArticle(article, key, StringUtils.join(result, ","));
					continue;
				}

				String text = page.getHtml().regex(rule).toString();
				buildArticle(article, key, text);
			}
			if ("html".equals(contentType) && "xpath".equals(selector)) {
				if ("pageNo".equals(key)) {
					String text = page.getHtml().xpath(rule).toString();
					if (StringUtils.isBlank(text)) {
						continue;
					}
					buildArticle(article, key, text.split("：")[1].trim());
					continue;
				}
				if ("subTitle".equals(key)) {
					List<String> strList = page.getHtml().xpath(rule).all();
					if (strList.size() >= 2) {
						buildArticle(article, key, strList.get(1));
					}
					continue;
				}
				if ("image".equals(key)) {
					List<String> text = page.getHtml().xpath(rule).all();
					buildArticle(article, key, StringUtils.join(text, ","));
					continue;
				}
				String text = page.getHtml().xpath(rule).toString();
				buildArticle(article, key, text);
			}
			if ("url".equals(contentType) && "xpath".equals(selector)) {
				buildArticle(article, key, page.getUrl().xpath(rule).toString());
			}
			if ("url".equals(contentType) && "regex".equals(selector)) {
				buildArticle(article, key, page.getUrl().regex(rule).toString());
			}
			if ("prefix".equals(key)) {
				article.setPrefix(selector);
			}
		}
		if (StringUtils.isBlank(article.getTitle())) {
			return;
		}
		String pageNo = article.getPageNo();
		Map<String, cn.cnki.spider.entity.Page> pageNewMap = this.getPageMap();
		if (null != pageNewMap && pageNewMap.containsKey(pageNo)) {
			cn.cnki.spider.entity.Page pageInfo = pageNewMap.get(pageNo);
			article.setJpg(pageInfo.getJpg());
			article.setPageName(pageInfo.getPageName());
		}
		String content = article.getContent();
		article.setContentAll(content);
		if (StringUtils.isBlank(content)) {
			page.putField("article", article);
			page.putField("directory", "QXNB");
			return;
		}
		String prefix = article.getPrefix();
		if (StringUtils.isBlank(prefix)) {
			page.putField("article", article);
			page.putField("directory", "QXNB");
			return;
		}
		content = content.replaceAll("\\.\\./\\.\\./\\.\\./", prefix);
		content = content.replaceAll("\\.\\./", "");
		article.setContent(content);
		article.setContentAll(content);
		page.putField("article", article);
		page.putField("directory", "QXNB");
	}

	private boolean doDigestCrawl(Page page, Map<String, Content> ruleMap) throws InterruptedException {
		String url = page.getUrl().toString();
		if (StringUtils.isBlank(url)) {
			return false;
		}
		String detailRegex = (".*content_\\d+\\.htm#article");
		// 详情页爬虫睡60s 确保文章列表执行完成
		if (url.matches(detailRegex)) {
			Thread.sleep(40 * 1000L);
			return true;
		}
		// http://epaper.scjjrb.com/Page/index/pageid/696998.html#696998
		// 设置全局 pageNoArticleListMap
		String articleListRegex = ".*qxnrb/html/.*/node_(\\d+)\\.htm";
		if (!url.matches(articleListRegex)) {
			return false;
		}
		buildPageIdArticleListMap(articleListRegex, url, ruleMap, page);
		return false;
	}

	private void buildPageIdArticleListMap(String regex, String url, Map<String, Content> ruleMap, Page page) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(url);
		String pageId = "";
		while (m.find()) {
			pageId = m.group(1);
		}
		final String newPageId = pageId;

		Content jpgContent = ruleMap.get("jpg");
		String jpgRule = jpgContent.getRule();
		String jpg = page.getHtml().xpath(jpgRule).toString();
		if (StringUtils.isNoneBlank(jpg)) {
			jpg = jpg.replace("../../../", "");
		}
		final String newJpg = jpg;

		Content pageNameContent = ruleMap.get("pageName");
		String pageNameRule = pageNameContent.getRule();
		List<String> pageNames = page.getHtml().xpath(pageNameRule).all();

		Content pageIdContent = ruleMap.get("pageId");
		String pageIdRule = pageIdContent.getRule();
		List<String> pathes = page.getHtml().xpath(pageIdRule).all();

		if (pageNames.isEmpty()) {
			return;
		}

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

		if (StringUtils.isBlank(jpg)) {
			return;
		}
		String urlRegex = ".*qxnrb/html/.*/node_(\\d+)\\.htm.*";

		pathes.forEach(path -> {
			if (!path.matches(urlRegex)) {
				return;
			}
//			if (!page.getUrl().toString().contains(newPageId)) {
//				return;
//			}
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

	private void buildArticle(SpiderArticle article, String key, String value) {
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
			if (!value.contains(",")) {
				article.setPageNo(buildPageNo(value));
				return;
			}
			article.setPageNo(buildPageNo(value.split(",")));
		}
		if ("pageName".equals(key)) {
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
			if (value.contains("：")) {
				String[] valueArr = value.split("：");
				if (valueArr.length < 2) {
					return;
				}
				value = value.split("：")[1];
			}
			if (value.contains(":")) {
				String[] valueArr = value.split(":");
				if (valueArr.length < 2) {
					return;
				}
				value = value.split(":")[1];
			}
			article.setAuthor(value);
		}
		if ("source".equals(key)) {
			if (value.contains("：")) {
				value = value.split("：")[1];
			}
			if (value.contains(":")) {
				value = value.split(":")[1];
			}
			article.setSource(value);
		}
		if ("content".equals(key)) {
			article.setContent(value);
		}
		if ("image".equals(key)) {
			article.setImage(doDirectoryFilter(value));
		}
	}

	private String doDirectoryFilter(String path) {
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
			return "02";
		}
		if ("第四版".equals(secondName)) {
			return "03";
		}
		if ("第五版".equals(secondName)) {
			return "04";
		}
		if ("第六版".equals(secondName)) {
			return "05";
		}
		if ("第七版".equals(secondName)) {
			return "06";
		}
		if ("第八版".equals(secondName)) {
			return "07";
		}
		if ("第九版".equals(secondName)) {
			return "08";
		}
		if ("第十版".equals(secondName)) {
			return "09";
		}
		if ("第十一版".equals(secondName)) {
			return "10";
		}
		if ("第十二版".equals(secondName)) {
			return "11";
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
				return date;
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
			return date.replaceAll("年|月|日", "");
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
		Pattern p = Pattern.compile(".*\\[CDATA\\[(.*)\\]\\].*");
		Matcher m = p.matcher(candidate);
		if (m.matches()) {
			return m.group(1).trim();
		}
		return "";
	}
}