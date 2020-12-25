package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.CSRankingDao;
import cn.cnki.spider.entity.CSRanking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CSRankingDaoImpl implements CSRankingDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public int batchInsert(List<CSRanking> records) {
        String sql = "insert ignore into crawl_csranking_copy (area, `range`, `domain`, direction, sub_direction," +
                "rank, institution, paper_count, faculty, `name`, pubs, adj, ctime)" +
                " values (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (CSRanking csRanking : records) {
            batchArgs.add(new Object[]{csRanking.getArea(), csRanking.getRange(),
                    csRanking.getDomain(), csRanking.getDirection(), csRanking.getSubDirection(),
                    csRanking.getRank(), csRanking.getInstitution(), csRanking.getPaperCount(),
                    csRanking.getFaculty(), csRanking.getName(), csRanking.getPubs(), csRanking.getAdj(),
                    csRanking.getCtime()});
        }
        jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }
}