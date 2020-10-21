package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.AACSBDao;
import cn.cnki.spider.entity.AACSB;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AACSBDaoImpl implements AACSBDao {

//    private final JdbcTemplate jdbcTemplate;

    @Override
    public int batchInsert(List<AACSB> records) {
        String sql = "insert ignore into crawl_aacsb (university, region, academy, accreditation, ctime, utime)" +
                " values (?,?,?,?,?,?)";

        List<Object[]> batchArgs = new ArrayList<>();
        for (AACSB aacsb : records) {
            batchArgs.add(new Object[]{aacsb.getUniversity(), aacsb.getRegion(),
                    aacsb.getAcademy(), aacsb.getAccreditation(),
                    System.currentTimeMillis(), System.currentTimeMillis()});
        }
        //jdbcTemplate.batchUpdate(sql, batchArgs);
        return records.size();
    }
}