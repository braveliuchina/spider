package cn.cnki.spider.common.controller;

import cn.cnki.spider.common.pojo.HtmlDO;
import cn.cnki.spider.common.pojo.HtmlVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CrawlHtmlRepository;
import cn.cnki.spider.common.service.CrawlServiceInterface;
import cn.cnki.spider.dao.RankingDao;
import cn.cnki.spider.entity.ReddotUrl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 爬虫Controller
 */

@Component
@RequiredArgsConstructor
public class Container {

    private final CrawlServiceInterface crawlService;

    private final RankingDao rankingDao;

    @Bean
    public void crawlItem() {

//        List<ReddotUrl> urlList = rankingDao.listUndo();
//        for (int i = 0; i< urlList.size(); i++) {
//            crawlService
//                    .topuniversitiesSeleniumCrawl("https://www.qschina.cn" + urlList.get(i).getUrl());
//        }

        crawlService
                .topuniversitiesSeleniumCrawl("https://www.qschina.cn/en/university-rankings/university-subject-rankings/2020/engineering-mechanical");

    }
}
