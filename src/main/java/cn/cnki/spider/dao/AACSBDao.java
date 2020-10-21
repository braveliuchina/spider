package cn.cnki.spider.dao;

import cn.cnki.spider.entity.AACSB;

import java.util.List;

public interface AACSBDao {

	public int batchInsert(List<AACSB> records);
	
}
