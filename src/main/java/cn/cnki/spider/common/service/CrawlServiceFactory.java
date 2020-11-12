package cn.cnki.spider.common.service;

import cn.cnki.spider.common.repository.CrawlHtmlRepository;
import cn.cnki.spider.dao.AACSBDao;
import cn.cnki.spider.dao.SpiderArticleDao;
import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.pipeline.ArticleDBBatchPageModelPipeline;
import cn.cnki.spider.pipeline.CommonPageModelPipeline;
import cn.cnki.spider.pipeline.ReddotDBItemBatchPageModelPipeline;
import cn.cnki.spider.pipeline.ReddotUrlDBBatchPageModelPipeline;
import cn.cnki.spider.scheduler.ScheduleJobRepository;
import cn.cnki.spider.spider.*;
import cn.cnki.spider.spider.AbstractNewspaperProcessor;
import cn.cnki.spider.spider.RedDotRepoProcessor;
import cn.cnki.spider.util.ChromeUtil;
import cn.cnki.spider.util.XmlDescriptorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlServiceFactory {

    private final ArticleDBBatchPageModelPipeline dbPageModelPipeline;

    private final ChromeUtil chromeUtil;

    private final SpiderArticleDao spiderArticleDao;

    private final XmlDescriptorUtil xmlDescriptor;

    private final AACSBDao aacsbDao;

    private final RedDotRepoProcessor redDotRepoProcessor;

    private final ReddotUrlDBBatchPageModelPipeline reddotUrlDBBatchPageModelPipeline;

    private final RedDotItemRepoProcessor redDotItemRepoProcessor;

    private final ReddotDBItemBatchPageModelPipeline reddotDBItemBatchPageModelPipeline;

    private final CommonRepoProcessor commonRepoProcessor;

    private final CommonPageModelPipeline commonPageModelPipeline;

    private final CrawlHtmlRepository crawlHtmlRepository;

    private final ScheduleJobRepository scheduleJobRepository;

    private final MongoTemplate mongoTemplate;

    private final SpiderConfigDao spiderConfigDao;

    public cn.cnki.spider.common.service.CrawlService buildCrawlService(AbstractNewspaperProcessor processor) {
        cn.cnki.spider.common.service.CrawlService service = new cn.cnki.spider.common.service.CrawlService(chromeUtil, dbPageModelPipeline,
                spiderArticleDao, xmlDescriptor, redDotRepoProcessor,
                reddotUrlDBBatchPageModelPipeline,
                redDotItemRepoProcessor,
                reddotDBItemBatchPageModelPipeline,
                commonRepoProcessor,
                commonPageModelPipeline,
                crawlHtmlRepository,
                mongoTemplate,
                scheduleJobRepository,
                spiderConfigDao,
                aacsbDao);
        service.setProcessor(processor);
        return service;
    }
}