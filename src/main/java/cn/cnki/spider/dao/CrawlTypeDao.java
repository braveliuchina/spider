package cn.cnki.spider.dao;

import cn.cnki.spider.entity.CrawlType;

import java.util.List;

public interface CrawlTypeDao {

    List<CrawlType> getTypes();
}
