package cn.cnki.spider.dao.impl;

import cn.cnki.spider.dao.CompanyDao;
import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.CompanyDO;
import cn.cnki.spider.entity.ReddotItem;
import cn.cnki.spider.entity.ReddotUrl;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyDaoImpl implements CompanyDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<CompanyDO> listUndo() {
        String sql = "SELECT top 1000 [companyGuid] as uid,[companyName] " +
                "  FROM [CnkiIndustryPlat].[dbo].[CompanyInfo] " +
                " where (listedCompany=1 or hasPatent=1 or highTechCompany=1) and isDone is null";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CompanyDO.class));
    }

    @Override
    public List<CompanyDO> listUndo(long start, long end) {
        String sql = "SELECT top 1000 [companyGuid] as uid,[companyName] " +
                "  FROM [CnkiIndustryPlat].[dbo].[CompanyInfo] " +
                " where (listedCompany=1 or hasPatent=1 or highTechCompany=1) and isDone is null " +
                "and id >= " + start + " and id <" + end ;
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CompanyDO.class));
    }

    @Override
    public void update(List<CompanyDO> companyDOList) {
        log.info("开始回写数据处理状态==========");
        String sql = "update [CnkiIndustryPlat].[dbo].[CompanyInfo] set isDone = ? where companyGuid = ?";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, "1");
                ps.setString(2, companyDOList.get(i).getUid());
            }

            @Override
            public int getBatchSize() {
                return companyDOList.size();
            }

        });
    }
//
//    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

}