package cn.cnki.spider.util;

import cn.cnki.spider.entity.SpiderArticle;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class XmlDescriptorUtil {

	private static final String XML_ARTICLE_FILE_NAME = "%s%s_ArticleTest.xml";

	private static final String XML_BAN_FILE_NAME = "%s%s_PlateTest.xml";

	/**
	 * 生成文章列表的xml描述文件
	 * 
	 * @param articles
	 */
	public void writeArticleXML(List<SpiderArticle> articles) {
		try {
			// 创建解析器工厂
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			Document document = builder.newDocument();
			// 不显示standalone="no"
			document.setXmlStandalone(true);
			Element articleInfoTest = document.createElement("ArticleInfoTest");
			Element articleInfoHtml = document.createElement("ArticleInfoHtml");
			Element articleInfos = document.createElement("ArticleInfos");
			
			if (null == articles || articles.isEmpty()) {
				return;
			}
			
			int index = 0;
			String path = "";
			String directory = "";
			String date = "";
			for (SpiderArticle article : articles) {
				path = article.getPath();
				directory = article.getDirectory();
				date = article.getDate();
				articleInfos.appendChild(buildArticleElement(article, document, index));
				index ++;
			}
			articleInfoTest.appendChild(articleInfoHtml);
			articleInfoTest.appendChild(articleInfos);
			document.appendChild(articleInfoTest);
			if (StringUtils.isAnyBlank(path, directory, date)) {
				return;
			}
			writeXML(document, path + String.format(XML_ARTICLE_FILE_NAME, directory, date));
			return;
		} catch (ParserConfigurationException e) {
			log.error("create xml document error", e);
		}
	}

	/**
	 * 生成版面信息的xml的描述文件
	 * 
	 * @param articles
	 */
	public void writeBanXML(List<SpiderArticle> articles) {
		try {
			// 创建解析器工厂
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;
			builder = factory.newDocumentBuilder();

			Document document = builder.newDocument();
			// 不显示standalone="no"
			document.setXmlStandalone(true);
			Element plateInfoTest = document.createElement("PlateInfoTest");
			Element plateHtml = document.createElement("PlateHtml");
			Element plateInfos = document.createElement("PlateInfos");
			
			if (null == articles || articles.isEmpty()) {
				return;
			}
			
			Map<String, List<SpiderArticle>> articlesMap = articles.stream().collect(Collectors.groupingBy(SpiderArticle::getPageNo));
			articlesMap = sortMapByKey(articlesMap);
			int index = 0;
			String path = "";
			String directory = "";
			String date = "";
			for (String key: articlesMap.keySet()) {
				List<SpiderArticle> articleList = articlesMap.get(key);
				if (null == articleList || articleList.isEmpty()) {
					continue;
				}
				SpiderArticle article = articleList.get(0);
				path = article.getPath();
				directory = article.getDirectory();
				date = article.getDate();
				plateInfos.appendChild(buildBanElement(article, document, index));
				index ++;
			}
			plateInfoTest.appendChild(plateHtml);
			plateInfoTest.appendChild(plateInfos);
			document.appendChild(plateInfoTest);
			if (StringUtils.isAnyBlank(path, directory, date)) {
				return;
			}
			writeXML(document, path + String.format(XML_BAN_FILE_NAME, directory, date));
			return;
		} catch (ParserConfigurationException e) {
			log.error("create xml document error", e);
		}
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param map
	 * @return
	 */
	public Map<String, List<SpiderArticle>> sortMapByKey(Map<String, List<SpiderArticle>> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}

		Map<String, List<SpiderArticle>> sortMap = new TreeMap<String, List<SpiderArticle>>(new MapKeyComparator());

		sortMap.putAll(map);

		return sortMap;
	}

	@NoArgsConstructor
	public class MapKeyComparator implements Comparator<String> {

		@Override
		public int compare(String str1, String str2) {

			return str1.compareTo(str2);
		}
	}
	
	private String buildPageNo(String pageNo) {
	    if (3 <= pageNo.length()) {
	    	return pageNo.substring(0, 3);
	    }
	    pageNo = pageNo.toUpperCase();
	    if (StringUtils.isBlank(pageNo)) {
	    	log.error("there is not page no build for this article");
	    	return "";
		}
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

	private Element buildBanElement(SpiderArticle article, Document document, int index) {
		Element plateInfo = document.createElement("PlateInfo");
		
		Element indexElement = document.createElement("Index");
		indexElement.setTextContent(String.valueOf(index));
		
		Element no = document.createElement("No");
		no.setTextContent(buildPageNo(article.getPageNo()));
		
		Element name = document.createElement("Name");
		name.setTextContent(article.getPageName());
		
		Element url = document.createElement("Url");
		
		Element pdf = document.createElement("PdfUrl");
		pdf.setTextContent(article.getPdf());
		
		Element phyPage = document.createElement("PhyPage");
		
		Element bodyContent = document.createElement("BodyContent");
		
		Element articleArea = document.createElement("ArticleArea");
		
		plateInfo.appendChild(indexElement);
		plateInfo.appendChild(no);
		plateInfo.appendChild(name);
		plateInfo.appendChild(url);
		plateInfo.appendChild(pdf);
		plateInfo.appendChild(phyPage);
		plateInfo.appendChild(bodyContent);
		plateInfo.appendChild(articleArea);
		
		return plateInfo;
	}
	
	private Element buildArticleElement(SpiderArticle article, Document document, int index) {
		// article
		Element articleInfo = document.createElement("ArticleInfo");
		
		Element indexElement = document.createElement("No");
		indexElement.setTextContent(article.getArticleNo());
		
		Element url = document.createElement("Url");
		
		Element title = document.createElement("Title");
		title.setTextContent(article.getTitle());
		
		Element subTitle = document.createElement("SubTitle");
		subTitle.setTextContent(article.getSubTitle());
		
		Element guideTitle = document.createElement("GuideTitle");
		guideTitle.setTextContent(article.getIntroTitle());
		
		Element author = document.createElement("Author");
		author.setTextContent(article.getAuthor());
		
		Element content = document.createElement("Content");
		content.setTextContent(article.getContent());
		
		// Plate
		Element plate = document.createElement("Plate");
		
		Element indexElement2 = document.createElement("Index");
		indexElement2.setTextContent(String.valueOf(index));
		
		Element no = document.createElement("No");
		no.setTextContent(buildPageNo(article.getPageNo()));
		
		Element name = document.createElement("Name");
		name.setTextContent(article.getPageName());
		
		Element urlElemnt = document.createElement("Url");
		urlElemnt.setTextContent(article.getArticleNo() + ".mht");
		
		Element pdf = document.createElement("PdfUrl");
		pdf.setTextContent(article.getPdf());
		
		Element phyPage = document.createElement("PhyPage");
		
		Element bodyContent = document.createElement("BodyContent");
		
		Element articleArea = document.createElement("ArticleArea");
		
		plate.appendChild(indexElement2);
		plate.appendChild(no);
		plate.appendChild(name);
		plate.appendChild(urlElemnt);
		plate.appendChild(pdf);
		plate.appendChild(phyPage);
		plate.appendChild(bodyContent);
		plate.appendChild(articleArea);
		
		
		articleInfo.appendChild(indexElement);
		articleInfo.appendChild(plate);
		articleInfo.appendChild(url);
		articleInfo.appendChild(title);
		articleInfo.appendChild(subTitle);
		articleInfo.appendChild(guideTitle);
		articleInfo.appendChild(author);
		articleInfo.appendChild(content);
		
		return articleInfo;
	}

	private void writeXML(Document document, String filePath) {
		try {
			// 创建TransformerFactory对象
			TransformerFactory factory = TransformerFactory.newInstance();
			// 创建 Transformer对象
			Transformer transformer = factory.newTransformer();
			// 输出内容是否使用换行
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// 创建xml文件并写入内容

			transformer.transform(new DOMSource(document), new StreamResult(new File(filePath)));
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			log.error("write xml error, {}", document);
		}
		log.info("生成xml文件成功: {}", filePath);
	}
}
