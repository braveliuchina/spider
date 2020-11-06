package cn.cnki.spider.dao;

import cn.cnki.spider.entity.ReddotItem;
import cn.cnki.spider.entity.ReddotUrl;

import java.util.List;

public interface ReddotDao {

    int urlBatchInsert(List<ReddotUrl> records);

    int itemBatchInsert(List<ReddotItem> records);

    List<ReddotUrl> listUndo(String domain);

    void updateStatusById(List<Long> ids);
}
