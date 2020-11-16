package cn.cnki.spider.pipeline;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.common.pojo.HistoryDO;
import cn.cnki.spider.common.repository.CommonCrawlHtmlRepository;
import cn.cnki.spider.dao.SpiderCommonDao;
import cn.cnki.spider.entity.CommonSpiderItem;
import cn.cnki.spider.scheduler.ScheduleJob;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import cn.cnki.spider.util.CopyUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
		String err = item.getErr();
		String hisId = item.getHisId();
		ScheduleJob job = saveAndUpdateJobAndHis(id, err, hisId);
//		Criteria criteria = Criteria.where(CommonHtmlDO.Fields.jobId).is(id).and(CommonHtmlDO.Fields.type).is("temp");
//		List<CommonHtmlDO> entityList = mongoTemplate.find(new Query().addCriteria(criteria), CommonHtmlDO.class);
//		if (!entityList.isEmpty()) {
//			mongoTemplate.remove(new Query().addCriteria(criteria), CommonHtmlDO.class);
//		}
		// 执行成功保存结果数据
		if ("2".equals(job.getJobStatus())) {
			item.setHisId(hisId);
			// 保存历史执行结果
			commonCrawlHtmlRepository.save(item);
		}

	}

	private ScheduleJob saveAndUpdateJobAndHis(long jobId, String err, String hisId) {
		long now = System.currentTimeMillis();
		Optional<ScheduleJob> jobOptional = scheduleJobRepository.findById(jobId);
		if (!jobOptional.isPresent()) {
			try {
				throw new Exception("there is no job with this id");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ScheduleJob job = jobOptional.get();
		Integer hisCount = job.getHis();
		// 异常情况
		if (StringUtils.isNotBlank(err)) {
			// 停止但执行失败
			job.setJobStatus("0");
			job.setErr(err);
		} else {
			// 停止但执行成功
			job.setJobStatus("2");
			job.setErr("");
			Integer result = job.getResult();
			if (null == result) {
				job.setResult(1);
			} else {
				job.setResult(++result);
			}
		}
		if (null == hisCount) {
			job.setHis(1);
		} else {
			job.setHis(++hisCount);
		}
		job.setUtime(System.currentTimeMillis());
		// 任务表
		scheduleJobRepository.saveAndFlush(job);

		// 更新任务执行记录表状态
		HistoryDO historyDO = new HistoryDO();
		historyDO.setId(hisId);
		historyDO.setJobId(job.getId());
		historyDO.setStatus(Integer.parseInt(job.getJobStatus()));
		historyDO.setErr(err);
		historyDO.setCtime(now);
		historyDO.setUtime(now);
		mongoTemplate.save(historyDO);
		return job;
	}

}
