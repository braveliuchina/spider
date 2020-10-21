package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderArticleDaoImpl implements SpiderArticleDao {

//	private final JdbcTemplate jdbcTemplate;
//
//	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public SpiderArticle insert(SpiderArticle article) {

		String sql = "insert ignore into TEST_NEWS_文章表 (protocal_id, date, cfg_id, page_no, page_name, intro_title,"
				+ "author, title, time, sub_title, source, zhuan_ban, property, content, content_all, image, jpg, pdf, article_no, ctime, utime) values ('"
				+ article.getProtocalId() + "','" + article.getDate() + "','" + article.getCfgId() + "','"
				+ article.getPageNo() + "','" + article.getPageName() + "','" + article.getIntroTitle() + "','"
				+ article.getAuthor() + "','" + article.getTitle() + "','" + article.getTime() + "','"
				+ article.getSubTitle() + "','" + article.getSource() + "','" + article.getZhuanBan() + "','"
				+ article.getProperty() + "','" + article.getContent() + "','" + article.getContentAll() + "','"
				+ article.getImage() + "','" + article.getJpg() + "','" + article.getPdf() + "','"
				+ article.getArticleNo() + "','" + article.getCtime() + "','" + article.getUtime() + "' )";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		long autoIncId = 0;

//		jdbcTemplate.update(new PreparedStatementCreator() {
//			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
//				PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
//				return ps;
//			}
//		}, keyHolder);

		autoIncId = keyHolder.getKey().longValue();
		article.setId(autoIncId);
		return article;
	}

	@Override
	public int batchInsert(List<SpiderArticle> articles) {
		String sql = "insert ignore into TEST_NEWS_文章表 (protocal_id, date, cfg_id, page_no, page_name, intro_title,"
				+ "author, title, time, sub_title, source, zhuan_ban, property, content, content_all, prefix, image, jpg, pdf, article_no, path, directory, ctime, utime) values ("
				+ "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		List<Object[]> batchArgs = new ArrayList<Object[]>();
		for (SpiderArticle article : articles) {
			batchArgs.add(new Object[] { article.getProtocalId(), article.getDate(), article.getCfgId(),
					article.getPageNo(), article.getPageName(), article.getIntroTitle(), article.getAuthor(),
					article.getTitle(), article.getTime(), article.getSubTitle(), article.getSource(),
					article.getZhuanBan(), article.getProperty(), article.getContent(), article.getContentAll(),
					article.getPrefix(), article.getImage(), article.getJpg(), article.getPdf(), article.getArticleNo(),
					article.getPath(), article.getDirectory(),
					article.getCtime(), article.getUtime() });
		}

//		jdbcTemplate.batchUpdate(sql, batchArgs);
		return 1;
	}

	@Override
	public List<SpiderArticle> queryListByProtocalAndDateStr(long id, String date) {

		String sql = "select * from TEST_NEWS_文章表  where protocal_id = ? and date = ? order by page_no asc";
		
//		List<SpiderArticle> articles = jdbcTemplate.query(sql, new Object[] {id, date},
//				new BeanPropertyRowMapper<>(SpiderArticle.class));
//		return articles;
		return Lists.newArrayList();
	}

	@Override
	public List<SpiderArticle> queryListByProtocalAndDateStr(List<Long> ids, String date) {
		String sql = "select * from TEST_NEWS_文章表  where protocal_id in ( :ids )  and date = :date order by page_no asc";
		Map<String, Object> paramsMap = new HashMap<>();
		paramsMap.put("date", date);
		paramsMap.put("ids", ids);
//		List<SpiderArticle> articles = namedParameterJdbcTemplate
//				.query(sql, paramsMap, new BeanPropertyRowMapper<>(SpiderArticle.class));
//		return articles;
		return Lists.newArrayList();
	}

}