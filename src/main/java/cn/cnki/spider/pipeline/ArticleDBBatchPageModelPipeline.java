package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.entity.SpiderArticle;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Component
@RequiredArgsConstructor
public class ArticleDBBatchPageModelPipeline implements Pipeline {

	private final SpiderArticleDao spiderArticleDao;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("articles");
		if (null == object) {
			processSingle(resultItemMap, task);
			return;
		}
		List<SpiderArticle> articles = (List<SpiderArticle>) object;
		if (articles.isEmpty()) {
			return;
		}
		articles = articles.stream()
				.filter(article -> null != article
						&& StringUtils.isNotBlank(article.getPageNo())
						&& StringUtils.isNotBlank(article.getArticleNo()))
				.collect(Collectors.toList());
		spiderArticleDao.batchInsert(articles);
	}

	private void processSingle(ResultItems resultItemMap, Task task) {
		Object object = resultItemMap.get("article");
		if (null == object) {
			return;
		}
		SpiderArticle article = (SpiderArticle) object;
		if (StringUtils.isBlank(article.getPageNo())
				|| StringUtils.isBlank(article.getArticleNo())) {
			return;
		}
		spiderArticleDao.batchInsert(Lists.newArrayList(article));
	}
}
