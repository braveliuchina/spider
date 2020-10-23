package cn.cnki.spider.common.repository;

import cn.cnki.spider.common.pojo.HtmlDO;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Html Repository
 */
@Repository
public interface CrawlHtmlRepository extends MongoRepository<HtmlDO, ObjectId> {
    public HtmlDO findByUrl(String url);
}
