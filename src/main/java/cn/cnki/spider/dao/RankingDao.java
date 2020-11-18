package cn.cnki.spider.dao;

import cn.cnki.spider.entity.RankingItem;
import cn.cnki.spider.entity.ReddotItem;
import cn.cnki.spider.entity.ReddotUrl;

import java.util.List;

public interface RankingDao {

    int urlBatchInsert(List<ReddotUrl> records);

    int itemBatchInsert(List<RankingItem> records);

    List<ReddotUrl> listUndo();

    void updateStatusById(List<Long> ids);
}
