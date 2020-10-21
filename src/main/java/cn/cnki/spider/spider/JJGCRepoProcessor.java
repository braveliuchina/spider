package cn.cnki.spider.spider;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.entity.SpiderConfig;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
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

@Data
@Component
public class JJGCRepoProcessor implements PageProcessor {

	private final SpiderConfigDao spiderConfigDao;

	private SpiderConfig spiderConfig;

	private Site site;
	
	private cn.cnki.spider.entity.Site siteRule;

	private Map<String, Content> spiderRuleMap;
	
	private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>(){};

	public JJGCRepoProcessor(SpiderConfigDao spiderConfigDao) {
		this.spiderConfigDao = spiderConfigDao;
	}

	@Override
	public Site getSite() {
		
		if (null != site && null != siteRule && null != spiderRuleMap) {
			return site;
		}
		this.setSpiderConfig(spiderConfigDao.getConfig("经济观察报"));
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

		Map<String, Content> ruleMap = getSpiderRuleMap();
		Content contentArticle = ruleMap.get("article");
		String ruleArticle = contentArticle.getRule();
		List<Selectable> nodes = null;
		String contentTypeArticle = contentArticle.getContentType();
		String selectorArticle = contentArticle.getSelector();
		if ("html".equals(contentTypeArticle) && "regex".equals(selectorArticle)) {
			nodes = page.getHtml().regex(ruleArticle).nodes();
		}else if ("html".equals(contentTypeArticle) && "xpath".equals(selectorArticle)) {
			nodes = page.getHtml().xpath(ruleArticle).nodes();
		}
		List<SpiderArticle> articles = Lists.newArrayList();
		long millis = System.currentTimeMillis();
        for(Selectable node : nodes){
        	SpiderArticle article = SpiderArticle.builder().build();
        	article.setProtocalId(spiderConfig.getId());
        	article.setCtime(millis);
        	article.setUtime(millis);
        	for(String key: ruleMap.keySet()) {
        		Content content = ruleMap.get(key);
        		String contentType = content.getContentType();
    			String selector = content.getSelector();
    			boolean links = content.isLinks();
    			String rule = content.getRule();
    			if ("html".equals(contentType) && "regex".equals(selector)) {
    				String text = node.regex(rule).toString();
    				if (content.isHead()) {
    					text = page.getHtml().regex(rule).toString();
    				}
    				buildArticle(article, key, text);
    			}
    			if ("html".equals(contentType) && "xpath".equals(selector)) {
    				if ("image".equals(key)) {
    					List<String> text = node.xpath(rule).all();
    					buildArticle(article, key, StringUtils.join(text, ","));
    					continue;
    				}
    				String text = node.xpath(rule).toString();
    				if (content.isHead()) {
    					text = page.getHtml().xpath(rule).toString();
    				}
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
        	String introTitle = article.getIntroTitle();
        	String author = article.getAuthor();
        	String title = article.getTitle();
        	String subTitle = article.getSubTitle();
        	String source = article.getSource();
        	String zhuanBan = article.getZhuanBan();
        	String property = article.getProperty();
        	String content = article.getContent();
        	
        	String contentNew = StringUtils.isBlank(content) ? "" :content.replace("\n", "<br/>");
        	String contentAll = "<div>" + introTitle + "</div>" +
        			"<div>" + author + "</div>" +
        			"<div>" + title + "</div>" +
        			"<div>" + subTitle + "</div>" +
        			"<div>" + source + "</div>" +
        			"<div>" + zhuanBan + "</div>" +
        			"<div>" + property + "</div>" +
        			"<div>" + contentNew + "</div>";
        
        	String img = article.getImage();
    		if (StringUtils.isBlank(img)) {
    			article.setContentAll(contentAll);
    			articles.add(article);	
    			continue;
    		}
    		String [] imgArr = img.split(",");
    		String date = article.getDate();
    		String prefix = article.getPrefix();
    		for (int i = 0; i < imgArr.length; i++) {
    			imgArr[i] = date + "/" + imgArr[i];
    			contentAll = contentAll + "<div><img src='" + prefix + imgArr[i] + "'/></div>";
    		}
    		article.setImage(StringUtils.join(imgArr, ","));
    		article.setContentAll(contentAll);
    		articles.add(article);	
        }
        page.putField("articles", articles);
        page.putField("directory", "JJGCB");
	}
	private void buildArticle(SpiderArticle article, String key, String value) {
		value = filterBlankSpecialChar(value);
		if ("date".equals(key)) {
			article.setDate(value);
		}
		if ("cfgId".equals(key)) {
			article.setCfgId(value);
		}
		if ("pageNo".equals(key)) {
			article.setPageNo(value);
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
			article.setImage(value);
		}
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
		if(m.matches()){
			return m.group(1).trim();
		}
		return ""; 
	}
}