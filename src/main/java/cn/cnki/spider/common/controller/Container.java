package cn.cnki.spider.common.controller;

import cn.cnki.spider.dao.CompanyDao;
import cn.cnki.spider.entity.BaiduBaikeSpiderItem;
import cn.cnki.spider.entity.CompanyDO;
import cn.cnki.spider.pipeline.BaiduBaikePageModelPipeline;
import cn.cnki.spider.spider.BaiduBaikeProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 爬虫Controller
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class Container {

    private final CompanyDao companyDao;

    private final BaiduBaikeProcessor processor;

    private final BaiduBaikePageModelPipeline baikePageModelPipeline;

    private final MongoTemplate mongoTemplate;

    @Bean
    public void crawlItem() throws UnsupportedEncodingException {
        long number = 0L;
        do {
            List<CompanyDO> companyDOList = companyDao.listUndo(14000000, 14111488);
            if (companyDOList.size() == 0) {
                break;
            }

//            CompanyDO companyDO1 = new CompanyDO();
//            companyDO1.setUid("000FFA66-83F2-4753-9E68-668A44AF6C39");
//            companyDO1.setCompanyName("上海伟星电机有限公司");
//
//            CompanyDO companyDO2 = new CompanyDO();
//            companyDO2.setUid("000F732A-32D0-4C8A-B4FD-AC4E1A0D168C");
//            companyDO2.setCompanyName("天津隆烨科技发展有限公司");
//            List<CompanyDO> companyDOList = Lists.newArrayList(companyDO1, companyDO2);
            log.info("开始处理--------------------------------------");
            WebDriver webDriver = null;
            try {
                File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
                System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

                ChromeOptions options = new ChromeOptions();
                webDriver = new ChromeDriver(options);
                webDriver.manage().window().setSize(new Dimension(1300, 800));

                for (CompanyDO companyDO : companyDOList) {
//                Spider baikeSpider = Spider.create(processor);
//                baikeSpider
//                        .addUrl("https://baike.baidu.com/item/" + URLEncoder
//                                .encode(companyDO.getCompanyName(), "UTF-8"))
//                        .addPipeline(baikePageModelPipeline).thread(1).run();

                    String url = "https://baike.baidu.com/item/" + URLEncoder
                            .encode(companyDO.getCompanyName(), "UTF-8");
                    webDriver.get(url);
                    // 等待已确保页面正常加载
//                    Thread.sleep(1000L);
                    String html = webDriver.getPageSource();
                    BaiduBaikeSpiderItem item = new BaiduBaikeSpiderItem();
                    item.setUid(companyDO.getUid());
                    item.setCompanyName(companyDO.getCompanyName());
                    long now = System.currentTimeMillis();
                    item.setCtime(now);
                    item.setUtime(now);
                    item.setUrl(url);
                    if (html.contains("百度百科错误页")) {
                        item.setExists(false);
                    }

                    item.setHtml(html);
                    try {
                        mongoTemplate.save(item);
                    } catch (DuplicateKeyException e) {
                    }

                }

            } catch (Exception e) {
                log.warn("number: " + number + "crawl exception err, would not update src data", e);
                return;
            } finally {
                if (null != webDriver) {
                    webDriver.quit();
                }
            }
//
//            if (null == urls || urls.isEmpty()) {
//                return;
//            }
//            for (String url : urls) {
//                detailSpider.addUrl(url).addPipeline(dbPageModelPipeline)
//                        .thread(thread).run();
//            }
            companyDao.update(companyDOList);
            number = number + companyDOList.size();
            log.info("-------------------" + number + "条数据处理完成---------------");
            if (companyDOList.size() < 100) {
                break;
            }
        } while (true);
    }
}
