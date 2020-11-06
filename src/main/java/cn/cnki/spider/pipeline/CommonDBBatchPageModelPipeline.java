package cn.cnki.spider.pipeline;

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
public class CommonDBBatchPageModelPipeline implements Pipeline {

	private final SpiderCommonDao spiderCommonDao;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("resultList");
		if (null == object) {
			return;
		}
		List<CommonSpiderItem> items = (List<CommonSpiderItem>) object;
		if (items.isEmpty()) {
			return;
		}
		spiderCommonDao.batchInsert(items);
	}

}
