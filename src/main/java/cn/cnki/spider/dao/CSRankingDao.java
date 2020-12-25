package cn.cnki.spider.dao;

import cn.cnki.spider.entity.AACSB;
import cn.cnki.spider.entity.CSRanking;

import java.util.List;

public interface CSRankingDao {

	public int batchInsert(List<CSRanking> records);
	
}
