package cn.cnki.spider.pipeline;

import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.util.Html2MHTCompiler;
import com.google.common.collect.Sets;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MhtFilePipeline extends FilePersistentBase implements Pipeline {

	public MhtFilePipeline() {
		setPath("C:/spider");
	}

	public MhtFilePipeline(String path) {
		setPath(path);
	}
	private Map<String, AtomicInteger> pageSerialNoMap = new ConcurrentHashMap<>();
	private Map<String, Set<String>> pageArticleTitleSetMap = new ConcurrentHashMap<>();
	
	@Override
	public void process(ResultItems resultItemsMap, Task task) {
		String directory = (String) resultItemsMap.get("directory");
		SpiderArticle article = (SpiderArticle) resultItemsMap.get("article");
		List<SpiderArticle> articles = (List<SpiderArticle>) resultItemsMap.get("articles");
		if (StringUtils.isBlank(directory)) {
			return;
		}
		if (null == article && null == articles) {
			return;
		}
		if (null != article) {
			doWriteSingleArticleMht(directory, article);
			return;
		}
		articles.forEach(single ->  doWriteSingleArticleMht(directory, single));
	}

	@Synchronized
	private boolean needAdd(String key, String title) {
		if (pageArticleTitleSetMap.containsKey(key)) {
			Set<String> titleSet = pageArticleTitleSetMap.get(key);
			if (titleSet.contains(title)) {
				return false;
			}
			titleSet.add(title);
			return true;
		}
		Set<String> resultSet = Sets.newConcurrentHashSet();
		resultSet.add(title);
		pageArticleTitleSetMap.put(key, resultSet);
		return true;
	}

	@Synchronized
	private void doWriteSingleArticleMht(String directory, SpiderArticle article) {
		article.setDirectory(directory);
		String pageNo = article.getPageNo();
		String date = article.getDate();
		String title = article.getTitle();
		int serialNo = 0;
		String pageKey = date + pageNo;
		boolean needAdd = needAdd(pageKey, title);
		if (!needAdd) {
			article = null;
			return;
		}
		if (pageSerialNoMap.containsKey(pageKey)) {
			serialNo = pageSerialNoMap.get(pageKey).addAndGet(1);
		}
		pageSerialNoMap.put(pageKey, new AtomicInteger(serialNo));
		if (StringUtils.isBlank(date)) {
			log.warn("日期为空");
			article = null;
			return;
		}
		if (StringUtils.isBlank(article.getPageNo())) {
			log.warn("版号为空");
			article = null;
			return;
		}
		String path = this.path + PATH_SEPERATOR + date + PATH_SEPERATOR + directory + PATH_SEPERATOR;
		article.setPath(path);
		String articleNo = directory + date + buildPageNo(article.getPageNo()) + buildSerialNo(serialNo);
		if (StringUtils.isBlank(articleNo) || "null".equals(articleNo)) {
			log.warn("文章号为空");
			article = null;
			return;
		}
		article.setArticleNo(articleNo);
		String fileName = articleNo + ".mht";

		String content = article.getContentAll();
		String image = article.getImage();
		if (StringUtils.isBlank(content)) {
			log.warn("文章内容不能为空,已忽略: {}", fileName);
			return;
		}
		List<String> imgList = Lists.newArrayList();
		if (StringUtils.isBlank(image)) {
			doWriteMhtFile(content, "gb2312", imgList, path, fileName);
			return;
		}
		String[] imgArr = image.split(",");
		String prefix = article.getPrefix();
		imgList = Arrays.asList(imgArr);
		imgList = imgList.stream().map(img -> {
			if (StringUtils.isBlank(prefix)) {
				return img;
			}
			if (!img.startsWith("http")) {
				return prefix + img;
			}
			return img;
		}).collect(Collectors.toList());
		doWriteMhtFile(content, "gb2312", imgList, path, fileName);
	}
	
	private String buildSerialNo(int number) {
		if (number <= 9) {
			return String.valueOf("" + number);
		}
		char startChar = 'A';
		return String.valueOf("" + (char)((int) (startChar) + (int)(number - 10)));
	}
	
	private String buildPageNo(String pageNo) {
	    if (3 <= pageNo.length()) {
	    	return pageNo.substring(0, 3);
	    }
	    pageNo = pageNo.toUpperCase();
	    char firstChar = pageNo.charAt(0);
	    // 字母开头
	    if (firstChar >= 'A' && firstChar <= 'Z') {
	    	String tempStr = pageNo.substring(1);
	    	pageNo = String.valueOf(firstChar) + "0" + tempStr;
	    	return pageNo;
	    }
	    if (pageNo.length() == 2) {
	    	return "0" + pageNo;
	    }
	    return "00" + pageNo;
	}
	private void doWriteMhtFile(String content, String encoding, List<String> imgList, String path, String fileName) {
		try {
			Html2MHTCompiler.createMhtArchive(content, Lists.newArrayList(), encoding, imgList,
					path + fileName);
		} catch (Exception e) {
			log.warn("创建mht文件异常: {}", fileName, e);
		}
	}
}