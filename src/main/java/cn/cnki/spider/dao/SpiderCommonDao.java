package cn.cnki.spider.dao;

import cn.cnki.spider.entity.CommonSpiderItem;
import cn.cnki.spider.entity.SpiderArticle;

import java.util.List;

public interface SpiderCommonDao {
	
	public int batchInsert(List<CommonSpiderItem> items);
	
	public List<SpiderArticle> queryListByProtocalAndDateStr(long id, String date);

	public List<SpiderArticle> queryListByProtocalAndDateStr(List<Long> ids, String date);
}
