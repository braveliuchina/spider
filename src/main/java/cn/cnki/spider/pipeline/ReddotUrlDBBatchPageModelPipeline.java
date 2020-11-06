package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.ReddotUrl;
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
public class ReddotUrlDBBatchPageModelPipeline implements Pipeline {

	private final ReddotDao reddotDao;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("urls");
		if (null == object) {
			return;
		}
		List<ReddotUrl> items = (List<ReddotUrl>) object;
		if (items.isEmpty()) {
			return;
		}
		reddotDao.urlBatchInsert(items);
	}

}
