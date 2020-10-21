package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

@Data
@Component
@RequiredArgsConstructor
public class FlashXmlDBPageModelPipeline implements Pipeline {

	private final SpiderArticleDao spiderArticleDao;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("articles");
		if (null == object) {
			return;
		}
		List<SpiderArticle> articles = (List<SpiderArticle>) object;
		if (articles.isEmpty()) {
			return;
		}
		spiderArticleDao.batchInsert(articles);
	}

}
