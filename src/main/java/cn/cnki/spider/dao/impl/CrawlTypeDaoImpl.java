package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.CrawlTypeDao;
import cn.cnki.spider.entity.CrawlType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlTypeDaoImpl implements CrawlTypeDao {

//	private final JdbcTemplate jdbcTemplate;

	@Override
	public List<CrawlType> getTypes() {
		String sql = "select * from crawl_type ";
		log.info("sql is :{}", sql);
//		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CrawlType.class));
		return Lists.newArrayList();
	}

}