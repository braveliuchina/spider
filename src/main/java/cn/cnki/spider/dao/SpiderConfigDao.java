package cn.cnki.spider.dao;

import cn.cnki.spider.entity.SpiderConfig;

import java.util.List;

public interface SpiderConfigDao {
	
	SpiderConfig getConfig(String name);

	SpiderConfig getConfigByCode(String code);

	SpiderConfig getConfigById(Long id);

	List<SpiderConfig> getConfigListByCode(String code);
}
