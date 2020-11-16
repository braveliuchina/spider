package cn.cnki.spider.common.service;

import cn.cnki.spider.common.repository.CrawlHtmlRepository;
import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.pipeline.ArticleMongoDBBatchPageModelPipeline;
import cn.cnki.spider.pipeline.CommonPageModelPipeline;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import cn.cnki.spider.spider.AbstractCloudNewspaperProcessor;
import cn.cnki.spider.spider.CommonRepoProcessor;
import cn.cnki.spider.util.ChromeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlServiceFactory {

    private final ArticleMongoDBBatchPageModelPipeline dbPageModelPipeline;

    private final CommonRepoProcessor commonRepoProcessor;

    private final CommonPageModelPipeline commonPageModelPipeline;

    private final ChromeUtil chromeUtil;

    private final CrawlHtmlRepository crawlHtmlRepository;

    private final MongoTemplate mongoTemplate;

    private final ScheduleJobRepository scheduleJobRepository;

    private final ProcessorFactory processorFactory;

    private final SpiderConfigDao spiderConfigDao;

    public cn.cnki.spider.common.service.CrawlService buildCrawlService(AbstractCloudNewspaperProcessor processor) {
        cn.cnki.spider.common.service.CrawlService service =
                new cn.cnki.spider.common.service.CrawlService(processorFactory, dbPageModelPipeline,
                        commonRepoProcessor,
                        commonPageModelPipeline,
                        chromeUtil,
                        crawlHtmlRepository,
                        mongoTemplate,
                        scheduleJobRepository,
                        spiderConfigDao);
        service.setProcessor(processor);
        return service;
    }
}