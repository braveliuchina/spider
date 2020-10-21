package cn.cnki.spider.dao;

import cn.cnki.spider.entity.SpiderArticle;

import java.util.List;

public interface SpiderArticleDao {
	
	public SpiderArticle insert(SpiderArticle article);
	
	public int batchInsert(List<SpiderArticle> articles);
	
	public List<SpiderArticle> queryListByProtocalAndDateStr(long id, String date);

	public List<SpiderArticle> queryListByProtocalAndDateStr(List<Long> ids, String date);
}
