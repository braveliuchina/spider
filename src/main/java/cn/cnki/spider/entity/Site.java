package cn.cnki.spider.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Site {
	
	private int retryTimes;
	
	private long sleepTime;
	
	private int cycleRetryTimes;

	private String charset;

	// 当处于url发现模式时 第一次请求的url的发现方式是否为直接寻找id的方式
	// 如中国市场监管报即为 此方式,此前遇到的绝大多数情况皆为日期匹配后拿对应id的方式,此时此字段即为false
	private boolean matchById;

	private String htmlXpathIdRule;

	private String idRegex;

	private String htmlXpathDayRule;

	private String htmlXpathARule;

	// yyyymmdd / dd
	private String htmlXpathMatchType;

	// match regex
	private String htmlXpathMatchRule;

	private String htmlXpathMatchReplaceRule;

	private String htmlXpathMatchUrlReplaceRule;

	// 发现url a标签前缀 如有则拼接到前面
	private String htmlXpathAPrefix;

	private String requestMethod;

	private Map<String, String> requestHeader;

	private Map<String, String> requestParam;

	private String flashXmlCfgAcceptHeader;

	private String discoveryUrl;

	private String discoveryType;

	private String judgeExistsUrl;

	private String discoverPageUrl;

	private String discoverArticleId;

	private String discoverPageNameUrl;

	private String discoverArticleContentUrl;

	private boolean gtId;

	private String gtIdFormat;

	private String prefix;
	
	private List<String> urls;
	
	private String url;

	// 此字段用以在获取入口url时是否需要进一步xpath获取页面信息,以供机器人调用
	private boolean pageUrlDeepInto;

	// 登录页面url 是否需要登录 如果此字段不为空,则需要使用selenium进行模拟登录
	private String loginUrl;

	private String userName;

	private String password;

	private int thread;
	
}