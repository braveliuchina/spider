package cn.cnki.spider.dao;

import cn.cnki.spider.entity.CompanyDO;
import cn.cnki.spider.entity.RankingItem;
import cn.cnki.spider.entity.ReddotUrl;

import java.util.List;

public interface CompanyDao {

    List<CompanyDO> listUndo();

    List<CompanyDO> listUndo(long start, long end);

    void update(List<CompanyDO> companyDOList);
}
