package cn.cnki.spider.common.controller;

import cn.cnki.spider.common.pojo.Result;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * 爬虫Controller
 */
public class CrawlController {

    @PostMapping("fetchHtmlContent")
    public Result<String> fetchHtmlContent() {
        return Result.of("ss");
    }

}
