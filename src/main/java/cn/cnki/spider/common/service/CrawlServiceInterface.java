package cn.cnki.spider.common.service;

import cn.cnki.spider.entity.ReddotUrl;

import java.util.List;

public interface CrawlServiceInterface {

    void crawlByDate(String year, String month, String day,
                     String code, String paperName) throws Exception;

    void crawlByDate(String year, String month, String day,
                     String code, String ... paperName) throws Exception;

    List<String> buildEnterUrl(String year, String month, String day,
                             String code, String paperName) throws Exception;

    List<String> buildEnterUrl(String year, String month, String day,
                     String code, String ... paperName) throws Exception;

    List<String> buildActualCrawlUrl(String year, String month, String day,
                               String code, String paperName) throws Exception;

    List<String> buildActualCrawlUrl(String year, String month, String day,
                               String code, String ... paperName) throws Exception;

    void seleniumCrawl(String url);

    String seleniumCrawlHtml(String url);

    void reddotUrlCrawl(String url, int size, String domain);

    void reddotItemCrawl(List<ReddotUrl> reddotUrls, String domain);

    void commonCrawl(String url, List<String> xpathList);
}
