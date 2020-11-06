package cn.cnki.spider.entity;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Content {
	
	// page.getUrl() or  page.getHtml() may be url/html
	private String contentType;
	
	// xpath regex
	private String selector;
	
	private String rule;

	// 预先处理的map列表是否需要将pageNo按正则处理
	private boolean pageNoArticleMapNeedMatch;

	// 预先处理的map列表是否需要将pageNo按正则处理
	private boolean pageNoArticleMapNeedMatchAll;

	// 是否持续抓取
	private boolean links;

	private boolean gt;

	private String gtRegex;

	private String gtId;

	// metaPrefix
	private String metaPrefix;

	// pageNo 在 第A01版: A1 此种情况下判断pageNo位置, 此情况pageNoIndex 为 0
	private String pageNoIndex;
	
	// mht文件目录存放目录
	private String directory;
	
	// 是否为头部属性 1对多的关系
	private boolean head = false;
	
	// xml 1 ... N 的关系 true为是
	private boolean xml = false;
	// 是否需要 暂存摘要 如日期 版次等 以便后续每篇文章统一使用
	private boolean digest = false;
	
	// 文章详情遍历时 需要忽略的属性列表
	private List<String> ignore;

	private boolean imgPrefix;
	// 文章内图片是否使用 prefix前缀
	private boolean imgPrefixUsePrefix;
	
	// 节点是否需要遍历多条 即Selectable元素 取.all()还是取 .toString()
	private boolean multi;

	private String matchValue;

	// 过滤替换正则, 如 图片前的../替换掉
	private String filterRegex;
	
	// 节点遍历多条时 拿取全部的节点List 还是 只使用 指定index的元素
	private boolean all = true;
	
	// 节点遍历多条时 只使用 指定index的元素时的元素index位置
	private int index;
	
	// page id == 版面信息  article要获取的字段 比如 digestKey 为 cfgId, 则根据 cfgId 去[文章id-版面信息]对应的map中查找 cfgId对应的key 并依据此key 拿到对应信息,并赋值 
	private String digestKey;

	// 版面汇总信息详情页地址url 正则表达式
	private String digestDetailPageUrlRegex;

	private Map<String, String> contentImgReplacer;
	
	// 版面汇总信息详情页地址url 正则表达式
	private Long detailPageCrawlThreadSleepMillis = 60 * 1000L;
		
	// 版面汇总信息列表页地址url 正则表达式
	private String digestListPageUrlRegex;
	
	private String digestDatePageUrlRegex;
	
	// 版面汇总信息url日期获取 正则表达式
	private String digestDateRegex;
	
	// 版号与版名分隔符 
	private String digestPageNoPageNameSplitChar;
	
	// 版面jpg图片前缀替换的正则表达式
	private String jpgPrefixReplaceRegex;
	
	// 版面jpg图片是否拼接date
	private boolean jpgConcatDate = false;

	// 版面jpg图片前缀替换的正则表达式
	private String pdfPrefixReplaceRegex;

	private boolean pdfPrefixUsePrefix;
	
	// pageId 和 pageList 地址 map 的类型, 如pageNo 意为 key为 pageNo  如pageId 意为pageId
	private String pageIdPageListMapType;


	private boolean pageNoScriptMatch;

	private String pageNoScriptMatchRegex;

	private int pageNoScriptMatchRegexIndex;

	private String articleIdScriptMatchRegex;

	private int articleIdScriptMatchRegexIndex;

}
