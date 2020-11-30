package cn.cnki.spider.pipeline;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.pojo.HistoryDO;
import cn.cnki.spider.common.repository.CommonCrawlHtmlRepository;
import cn.cnki.spider.entity.BaiduBaikeSpiderItem;
import cn.cnki.spider.scheduler.ScheduleJob;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Optional;

@Data
@Component
@RequiredArgsConstructor
public class BaiduBaikePageModelPipeline implements Pipeline {

	private final MongoTemplate mongoTemplate;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("result");
		if (null == object) {
			return;
		}
		BaiduBaikeSpiderItem item = (BaiduBaikeSpiderItem) object;
		mongoTemplate.save(item);

	}

}
