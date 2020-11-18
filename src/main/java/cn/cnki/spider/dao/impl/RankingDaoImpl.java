package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.RankingDao;
import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.RankingItem;
import cn.cnki.spider.entity.ReddotItem;
import cn.cnki.spider.entity.ReddotUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingDaoImpl implements RankingDao {

    private final JdbcTemplate jdbcTemplate;
//
//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int urlBatchInsert(List<ReddotUrl> records) {
        String sql = "insert ignore into crawl_ranking_item_url_copy (`type`, `url`, `done`, `ctime`, `utime`)" +
                " values (?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (ReddotUrl urlObj : records) {
            batchArgs.add(new Object[]{urlObj.getType(), urlObj.getUrl(), urlObj.getDone(),
                    System.currentTimeMillis(), System.currentTimeMillis()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }

    @Override
    public int itemBatchInsert(List<RankingItem> records) {
        String sql = "insert ignore into crawl_ranking_copy (`rank`, `institution`, `score`, " +
                "`subject`, `year`)" +
                " values (?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (RankingItem item : records) {
            batchArgs.add(new Object[]{item.getRank(), item.getInstitution(),
                    item.getScore(), item.getSubject(), item.getYear()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }

    @Override
    public List<ReddotUrl> listUndo() {
        String sql = "select * from crawl_ranking_item_url_copy ";
        return jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<>(ReddotUrl.class));
//        return Lists.newArrayList();
    }

    @Override
    public void updateStatusById(List<Long> ids) {


        String sql = "update crawl_reddot_item_url set done = '1' where id = ?";
////        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                ps.setLong(1, ids.get(i));
//            }
//
//            @Override
//            public int getBatchSize() {
//                return ids.size();
//            }
//
//        });
    }
}