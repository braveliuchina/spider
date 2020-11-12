package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.entity.SpiderConfig;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderConfigDaoImpl implements SpiderConfigDao {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public SpiderConfig getConfig(String name) {
		String sql = "select * from TEST_NEWS_协议表 where name = '" + name + "'";
		log.info("sql is :{}", sql);
//		return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SpiderConfig.class));
		SpiderConfig config = new SpiderConfig();
		config.setSite(new JSONObject().toJSONString());
		return config;
	}

	@Override
	public SpiderConfig getConfigByCode(String code) {
		String sql = "select * from TEST_NEWS_协议表 where code = '" + code + "'";
		log.info("sql is :{}", sql);
//		return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(SpiderConfig.class));
		SpiderConfig config = new SpiderConfig();
		config.setSite(new JSONObject().toJSONString());
		return new SpiderConfig();
	}

	@Override
	public SpiderConfig getConfigById(Long id) {
		String sql = "select * from TEST_NEWS_协议表 where id = '" + id + "'";
		log.info("sql is :{}", sql);
		List<SpiderConfig> configs = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SpiderConfig.class));
		if (configs.isEmpty()) {
			return null;
		}
		return configs.get(1);
	}

	@Override
	public List<SpiderConfig> getConfigListByCode(String code) {
		String sql = "select * from TEST_NEWS_协议表 where code = '" + code + "'";
		log.info("sql is :{}", sql);
//		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SpiderConfig.class));
		return Lists.newArrayList();
	}

}