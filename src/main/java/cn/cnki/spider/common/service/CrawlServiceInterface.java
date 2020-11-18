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

    void seleniumCrawlHtmlAndSave(long jobId, String type, String hisId, String url);

    void commonCrawl(String url, List<String> xpathList);

    void commonCrawlV2(long jobId, String type, String hisId, String url, List<String> xpathList);

    void templateCrawl(long jobId, String type, String hisId, long templateId);

    void rankingUrlCrawl();

    void rankingItemCrawl();

    void topuniversitiesSeleniumCrawl(String url);
}
