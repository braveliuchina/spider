package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.SpiderCommonDao;
import cn.cnki.spider.entity.CommonSpiderItem;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpiderCommonDaoImpl implements SpiderCommonDao {

//    private final JdbcTemplate jdbcTemplate;
//
//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int batchInsert(List<CommonSpiderItem> items) {
//        String sql = "insert ignore into crawl_result (xpath_id, temp, result, ctime, utime) values ("
//                + "?,?,?,?,?)";
//
//        List<Object[]> batchArgs = new ArrayList<Object[]>();
//        for (CommonSpiderItem item : items) {
//            batchArgs.add(new Object[]{item.getXpathId(), item.getTemp(), item.getResult(),
//                    item.getCtime(), item.getUtime()});
//        }
//
//        jdbcTemplate.batchUpdate(sql, batchArgs);
        return 1;
    }

    @Override
    public List<SpiderArticle> queryListByProtocalAndDateStr(long id, String date) {

        String sql = "select * from TEST_NEWS_文章表  where protocal_id = ? and date = ? order by page_no asc";

//        List<SpiderArticle> articles = jdbcTemplate.query(sql, new Object[]{id, date},
//                new BeanPropertyRowMapper<>(SpiderArticle.class));
        return Lists.newArrayList();
    }

    @Override
    public List<SpiderArticle> queryListByProtocalAndDateStr(List<Long> ids, String date) {
//        String sql = "select * from TEST_NEWS_文章表  where protocal_id in ( :ids )  and date = :date order by page_no asc";
//        Map<String, Object> paramsMap = new HashMap<>();
//        paramsMap.put("date", date);
//        paramsMap.put("ids", ids);
//        List<SpiderArticle> articles = namedParameterJdbcTemplate
//                .query(sql, paramsMap, new BeanPropertyRowMapper<>(SpiderArticle.class));
//        return articles;
          return Lists.newArrayList();
    }

}