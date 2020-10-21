package cn.cnki.spider.common.service;

import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.entity.SpiderArticle;
import cn.cnki.spider.util.XmlDescriptorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class XmlServiceClass {
	
	private final SpiderArticleDao spiderArticleDao;

	private final XmlDescriptorUtil xmlDescriptor;

	public void buildXml(long id, String dateStr) throws Exception {
		List<SpiderArticle> articles = spiderArticleDao.queryListByProtocalAndDateStr(id, dateStr);

		if (articles.isEmpty()) {
			log.warn("generate xml failed: article list is empty, protocalId: {}, dateStr : {}", id, dateStr);
			throw new Exception("article list is empty, please check if there's page for this date");
		}
		xmlDescriptor.writeBanXML(articles);
		xmlDescriptor.writeArticleXML(articles);
	}


	public void buildXml(List<Long> ids, String dateStr) {
		List<SpiderArticle> articles = spiderArticleDao.queryListByProtocalAndDateStr(ids, dateStr);

		if (articles.isEmpty()) {
			log.warn("generate xml failed: article list is empty, protocalId: {}, dateStr : {}", ids, dateStr);
			return;
		}
		xmlDescriptor.writeBanXML(articles);
		xmlDescriptor.writeArticleXML(articles);
	}
}
