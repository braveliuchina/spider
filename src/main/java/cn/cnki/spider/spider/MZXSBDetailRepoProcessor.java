package cn.cnki.spider.spider;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.entity.SpiderConfig;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
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

@Data
//@Component
public class MZXSBDetailRepoProcessor implements PageProcessor {

	private final SpiderConfigDao spiderConfigDao;

	private SpiderConfig spiderConfig;

	private Site site;

	private cn.cnki.spider.entity.Site siteRule;

	private Map<String, Content> spiderRuleMap;

	private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>(){};
	
	public MZXSBDetailRepoProcessor(SpiderConfigDao spiderConfigDao) {
		this.spiderConfigDao = spiderConfigDao;
	}

	@Override
	public Site getSite() {

		if (null != site && null != siteRule && null != spiderRuleMap) {
			return site;
		}
		this.setSpiderConfig(spiderConfigDao.getConfig("民主协商报"));
		String json = spiderConfig.getSite();
		String contentJson = spiderConfig.getContent();
		siteRule = JSONObject.parseObject(json, cn.cnki.spider.entity.Site.class);
		HashMap<String, Content> content = (HashMap<String, Content>) JSONObject.parseObject(contentJson, typeReference);
		spiderRuleMap = content;
		site = Site.me().setRetryTimes(siteRule.getRetryTimes())
				.setSleepTime(((Long) (siteRule.getSleepTime())).intValue());
		return site;

	}

	@Override
	public void process(Page page) {

		String html = page.getHtml().toString();
		Map<String, Content> ruleMap = getSpiderRuleMap();
		Content contentArticle = ruleMap.get("article");
		String ruleArticle = contentArticle.getRule();
		String contentTypeArticle = contentArticle.getContentType();
		String selectorArticle = contentArticle.getSelector();
		boolean isLink = contentArticle.isLinks();
		if (isLink && "html".equals(contentTypeArticle) && "regex".equals(selectorArticle)) {
			page.addTargetRequests(page.getHtml().links().regex(ruleArticle).all());
		} else if (isLink && "html".equals(contentTypeArticle) && "xpath".equals(selectorArticle)) {
			page.addTargetRequests(page.getHtml().links().xpath(ruleArticle).all());
		}
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
			if ("article".equals(key)) {
				continue;
			}
			if ("html".equals(contentType) && "regex".equals(selector)) {
				if ("image".equals(key)) {
					List<String> text = page.getHtml().regex(rule).all();
					List<String> result = text.stream().map(s -> s.replaceAll("../../", "")).collect(Collectors.toList());
					buildArticle(article, key, StringUtils.join(result, ","));
					continue;
				}
				
				String text = page.getHtml().regex(rule).toString();
				buildArticle(article, key, text);
			}
			if ("html".equals(contentType) && "xpath".equals(selector)) {
				if ("pageNo".equals(key)) {
					List<String> text = page.getHtml().xpath(rule).all();
					buildArticle(article, key, StringUtils.join(text, ","));
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
		String prefix = article.getPrefix();
		if (StringUtils.isBlank(prefix)) {
			page.putField("article", article);
			page.putField("directory", "MZXSB");
			return;
		}
		String content = article.getContent();
		if (StringUtils.isBlank(content)) {
			page.putField("article", article);
			page.putField("directory", "MZXSB");
			return;
		}
		content = content.replaceAll("\\.\\./\\.\\./", prefix);
		article.setContent(content);
		article.setContentAll(content);
		page.putField("article", article);
		page.putField("directory", "MZXSB");
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
			article.setAuthor(value);
		}
		if ("source".equals(key)) {
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
	
	private String buildPageNo(String ... args) {
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
			String [] dateArr = date.split("/");
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