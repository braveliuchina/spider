package cn.cnki.spider.pipeline;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.repository.CommonCrawlHtmlRepository;
import cn.cnki.spider.dao.SpiderCommonDao;
import cn.cnki.spider.entity.CommonSpiderItem;
import cn.cnki.spider.scheduler.ScheduleJob;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import cn.cnki.spider.util.CopyUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.Optional;

@Data
@Component
@RequiredArgsConstructor
public class CommonPageModelPipeline implements Pipeline {

	private final CommonCrawlHtmlRepository commonCrawlHtmlRepository;

	private final MongoTemplate mongoTemplate;

	private final ScheduleJobRepository scheduleJobRepository;

	@Override
	public void process(ResultItems resultItemMap, Task task) {

		Object object = resultItemMap.get("item");
		if (null == object) {
			return;
		}
		CommonHtmlDO item = (CommonHtmlDO) object;
		long id = item.getJobId();

		Optional<ScheduleJob> jobOptional = scheduleJobRepository.findById(id);
		if (!jobOptional.isPresent()) {
			try {
				throw new Exception("there is no job with this id");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ScheduleJob job = jobOptional.get();
		job.setUtime(System.currentTimeMillis());
		job.setJobStatus("2");
		scheduleJobRepository.saveAndFlush(job);
//		Criteria criteria = Criteria.where(CommonHtmlDO.Fields.jobId).is(id).and(CommonHtmlDO.Fields.type).is("temp");
//		List<CommonHtmlDO> entityList = mongoTemplate.find(new Query().addCriteria(criteria), CommonHtmlDO.class);
//		if (!entityList.isEmpty()) {
//			mongoTemplate.remove(new Query().addCriteria(criteria), CommonHtmlDO.class);
//		}
		commonCrawlHtmlRepository.save(item);
	}

}
