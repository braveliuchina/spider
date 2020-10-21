package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.ReddotItem;
import cn.cnki.spider.entity.ReddotUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReddotDaoImpl implements ReddotDao {

//    private final JdbcTemplate jdbcTemplate;
//
//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public int urlBatchInsert(List<ReddotUrl> records) {
        String sql = "insert ignore into crawl_reddot_item_url (`type`, `url`, `done`, `ctime`, `utime`)" +
                " values (?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (ReddotUrl urlObj : records) {
            batchArgs.add(new Object[]{urlObj.getType(), urlObj.getUrl(), urlObj.getDone(),
                    System.currentTimeMillis(), System.currentTimeMillis()});
        }
//        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }

    @Override
    public int itemBatchInsert(List<ReddotItem> records) {
        String sql = "insert ignore into crawl_reddot_item (`domain`, `item_name`, `item_type`, " +
                "`award_type`, `award_year`, `manufacturer`, `in_house_design`, `design`, `url`, `ctime`, `utime`)" +
                " values (?,?,?,?,?,?,?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (ReddotItem item : records) {
            batchArgs.add(new Object[]{item.getDomain(), item.getItemName(),
                    item.getItemType(), item.getAwardType(), item.getAwardYear(), item.getManufacturer(),
                    item.getInHouseDesign(), item.getDesign(), item.getUrl(),
                    System.currentTimeMillis(), System.currentTimeMillis()});
        }
//        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }

    @Override
    public List<ReddotUrl> listUndo(String domain) {
        String sql = "select * from crawl_reddot_item_url where type ='" + domain +"'" +
                " and done = '0' limit 100";
//        return jdbcTemplate.query(sql, new Object[]{}, new BeanPropertyRowMapper<>(ReddotUrl.class));
        return Lists.newArrayList();
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