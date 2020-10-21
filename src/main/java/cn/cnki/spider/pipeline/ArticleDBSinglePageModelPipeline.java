package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Data
@Component
@RequiredArgsConstructor
public class ArticleDBSinglePageModelPipeline implements Pipeline {

	private final SpiderArticleDao spiderArticleDao;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("article");
		if (null == object) {
			return;
		}
		SpiderArticle article = (SpiderArticle) object;
		if (null == article) {
			return;
		}
		spiderArticleDao.batchInsert(Lists.newArrayList(article));
	}

}
