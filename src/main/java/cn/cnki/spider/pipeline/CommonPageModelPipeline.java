package cn.cnki.spider.pipeline;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.repository.CommonCrawlHtmlRepository;
import cn.cnki.spider.dao.SpiderCommonDao;
import cn.cnki.spider.entity.CommonSpiderItem;
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
public class CommonPageModelPipeline implements Pipeline {

	private final CommonCrawlHtmlRepository commonCrawlHtmlRepository;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("item");
		if (null == object) {
			return;
		}
		CommonHtmlDO item = (CommonHtmlDO) object;
		commonCrawlHtmlRepository.save(item);
	}

}
