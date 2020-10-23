package cn.cnki.spider.common.controller;

import cn.cnki.spider.common.pojo.HtmlDO;
import cn.cnki.spider.common.pojo.HtmlVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CrawlHtmlRepository;
import cn.cnki.spider.common.service.CrawlServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 爬虫Controller
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("crawl")
public class CrawlController {

    private final CrawlServiceInterface crawlService;

    private final CrawlHtmlRepository crawlHtmlRepository;

    private final MongoTemplate mongoTemplate;

    @PostMapping("fetchHtmlContent")
    public Result<String> fetchHtmlContent(@RequestBody HtmlVo enterVO) {

        String url = enterVO.getUrl();

        if (StringUtils.isEmpty(url)) {
            return new Result("", false, "请传递url");
        }
        String htmlContent = crawlService.seleniumCrawlHtml(url);
        long now = System.currentTimeMillis();
        HtmlDO htmlDO = new HtmlDO();
        htmlDO.setUrl(url);
        htmlDO.setHtml(htmlContent);
        htmlDO.setCtime(now);
        htmlDO.setUtime(now);
        HtmlDO newHtmlDO = crawlHtmlRepository.findByUrl(url);
        if (null != newHtmlDO) {
            Query query = new Query();
            query.addCriteria(Criteria.where("url").is(url));
            Update update = new Update();
            update.set("html", htmlContent);
            update.set("utime", now);
            mongoTemplate.upsert(query, update, HtmlDO.class);
        } else {
            crawlHtmlRepository.save(htmlDO);
        }

        return Result.of("html string saved successfully");
    }

}
