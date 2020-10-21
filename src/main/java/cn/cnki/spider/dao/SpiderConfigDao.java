package cn.cnki.spider.dao;

import cn.cnki.spider.entity.SpiderConfig;

import java.util.List;

public interface SpiderConfigDao {
	
	SpiderConfig getConfig(String name);

	SpiderConfig getConfigByCode(String code);

	List<SpiderConfig> getConfigListByCode(String code);
}
