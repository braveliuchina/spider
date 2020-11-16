package cn.cnki.spider.pipeline;

import cn.cnki.spider.common.pojo.ArticleDO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Data
@Component
@RequiredArgsConstructor
public class ArticleMongoDBBatchPageModelPipeline implements Pipeline {

    private final MongoTemplate mongoTemplate;

    @Override
    public void process(ResultItems resultItemMap, Task task) {

        Object object = resultItemMap.get("articles");
        if (null == object) {
			processSingle(resultItemMap, task);
            return;
        }
        List<ArticleDO> articles = (List<ArticleDO>) object;
        if (articles.isEmpty()) {
            return;
        }
        mongoTemplate.insert(articles, ArticleDO.class);

    }

    private void processSingle(ResultItems resultItemMap, Task task) {
        Object object = resultItemMap.get("article");
        if (null == object) {
            return;
        }
        mongoTemplate.save((ArticleDO)object);
    }
}
