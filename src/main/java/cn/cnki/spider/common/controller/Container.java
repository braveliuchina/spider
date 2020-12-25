package cn.cnki.spider.common.controller;

import cn.cnki.spider.dao.CSRankingDao;
import cn.cnki.spider.entity.CSRanking;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 爬虫Controller
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class Container {

    private final CSRankingDao csRankingDao;

    private final MongoTemplate mongoTemplate;

    private List<String> areaList;

    @Bean
    public void crawlItem() throws UnsupportedEncodingException, InterruptedException {
//        doCSRankingCrawl();
    }

    private List<WebElement> buildTbodyElement(List<WebElement> directions, WebDriver driver) {
        if (null == directions || directions.isEmpty()) {
            return directions;
        }
        List<WebElement> result = Lists.newArrayList();
        for (WebElement element: directions) {
            try {
                WebElement elements = element.findElement(By.tagName("input"));
                result.add(element);
            } catch (NoSuchElementException e) {
                continue;
            }
        }
        return result;
    }
    private void doCSRankingInner(String url) throws InterruptedException {
        WebDriver webDriver = null;

        String range = "1970-1980";
        if (url.contains("80") && url.contains("90")) {
            range = "1980-1990";
        }
        if (url.contains("90") && url.contains("00")) {
            range = "1990-2000";
        }
        if (url.contains("00") && url.contains("10")) {
            range = "2000-2010";
        }
        if (url.contains("10") && url.contains("20") && !url.contains("00")) {
            range = "2010-2020";
        }
        String area = "the USA";
        if (url.contains("canada")) {
            area = "Canada";
        }
        if (url.contains("northamerica")) {
            area = "USA and Canada";
        }
        if (url.contains("southamerica")) {
            area = "South America";
        }
        if (url.contains("australasia")) {
            area = "Australasia";
        }
        if (url.contains("asia") && !url.contains("australasia")) {
            area = "Asia";
        }
        if (url.contains("europe")) {
            area = "Europe";
        }
        if (url.contains("africa")) {
            area = "Africa";
        }
        if (url.contains("world")) {
            area = "the world";
        }
        File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

        ChromeOptions options = new ChromeOptions();
        webDriver = new ChromeDriver(options);
        webDriver.manage().window().setSize(new Dimension(1300, 800));
        webDriver.get(url);
        webDriver.get(url);
        // 等待已确保页面正常加载
        Thread.sleep(25000L);
        try {


            doCSRankingCrawlV2(range, area, webDriver);

        } catch (ElementNotInteractableException e100) {
            log.info("try 2nd times..................................");
            webDriver.get(url);
            // 等待已确保页面正常加载
            Thread.sleep(20000L);
            try {
                doCSRankingCrawlV2(range, area, webDriver);
            } catch(ElementNotInteractableException e200) {
                log.info("try 3rd times..................................");
                webDriver.get(url);
                // 等待已确保页面正常加载
                Thread.sleep(20000L);
                doCSRankingCrawlV2(range, area, webDriver);
            }
        } catch (Exception e100) {
            log.info("err for url:" + url, e100);
        } finally {
            if (null != webDriver) {
                webDriver.quit();
            }
        }
    }

    private void doCSRankingCrawlV2(String range, String area, WebDriver webDriver) {

            long now = System.currentTimeMillis();
            List<CSRanking> rankings = Lists.newArrayList();
            List<WebElement> elements = webDriver.findElements(By.xpath("//th[@colspan]/p[@class='text-muted']"));
            List<WebElement> directions = webDriver.findElement(By.cssSelector("table.table-striped")).findElements(By.xpath("*"));
            directions = buildTbodyElement(directions, webDriver);
            int i = 0;
//            elements = elements.subList(1, elements.size());
            for (WebElement domainElement : elements) {
                String domain = domainElement.getText();
                if (!StringUtils.isBlank(domain)) {
                    domain = domain.split(" ")[0];
                }
                if (!"Interdisciplinary".equals(domain) ) {
                    i++;
                    continue;
                }
                log.info("range : {}, location: {}, domain : {}", range, area, domain);
                List<WebElement> trList = directions.get(i).findElements(By.tagName("tr"));
                for (WebElement tr : trList) {
                    String contentTr = tr.getText();
                    if (StringUtils.isBlank(contentTr)) {
                        continue;
                    }
                    String direction = "";
                    try {
                        direction = tr.findElement(By.tagName("label")).getText();
                    } catch (NoSuchElementException e) {
                        direction = tr.findElement(By.tagName("td")).getText();
                        if (!StringUtils.isBlank(direction)) {
                            direction = direction.substring(1);
                        }
                    }
                    direction = direction.trim();
//                    if (!"Visualization".equals(direction)) {
//                        continue;
//                    }
                    WebElement triangle = tr.findElement(By.className("hovertip"));
                    List<WebElement> subTrs = tr.findElements(By.cssSelector("div.table-responsive table.table-sm tbody tr td table tbody tr"));
                    triangle.click();
                    if (subTrs.isEmpty()) {
                        triangle.click();
                        continue;
                    }
                    subTrs = subTrs.subList(1, subTrs.size());
                    for (WebElement subTr : subTrs) {
                        String subDirection;
                        try {
                            subDirection = subTr.findElement(By.tagName("a")).getText();
                        } catch (NoSuchElementException e) {
                            log.info("-------");
                            continue;
                        }
//                        if (!"VR".equals(subDirection)) {
//                            continue;
//                        }
                        WebElement checkbox = subTr.findElement(By.tagName("input"));
                        checkbox.click();

                        List<WebElement> institutionTrs = webDriver.findElements(By.xpath("//table[@id='ranking']/tbody//tr"));
                        if (institutionTrs.isEmpty()) {
                            checkbox.click();
                            continue;
                        }
                        // 滚动条滚动
                        if (institutionTrs.size()> 60) {
                            WebElement scrollElement = webDriver.findElement(By.xpath("//div[@id='success']/div[@class='table-responsive']"));
                            JavascriptExecutor js = ( JavascriptExecutor ) webDriver;
                            js.executeScript("document.getElementById(\"success\").firstChild.scrollTop=10000", scrollElement);
                            institutionTrs = webDriver.findElements(By.xpath("//table[@id='ranking']/tbody//tr"));
                        }

                        int rank = 0;
                        String institution = "";
                        String paperCount = "";
                        String faculty = "";
                        String name = "";
                        String pubs = "";
                        String adj = "";
//                        institutionTrs = institutionTrs.subList(260, institutionTrs.size());
                        for (WebElement institutionTr : institutionTrs) {

                            List<WebElement> tdList = institutionTr.findElements(By.xpath("td"));
                            if (4 == tdList.size()) {
                                try {
                                    WebElement element1 = tdList.get(1).findElement(By.tagName("small"));
                                } catch (NoSuchElementException e) {
                                    rank = 0;
                                    institution = "";
                                    paperCount = "";
                                    faculty = "";
                                    name = "";
                                    pubs = "";
                                    adj = "";
                                }

                            }
                            String rankStr = institutionTr.findElement(By.xpath("td[1]")).getText().trim();
                            if (!StringUtils.isBlank(rankStr)) {
                                try {
                                    rank = Integer
                                            .parseInt(rankStr);
                                } catch (NumberFormatException e) {

                                }
                            }
                            try {
                                if (StringUtils.isBlank(institution)) {
                                    institution = institutionTr.findElement(By.xpath("td[2]")).getText().trim();
                                    institution = institution.substring(1);
                                }
                                WebElement triangle2 = institutionTr.findElement(By.xpath("td/span[@class='hovertip']"));
                                triangle2.click();
                                paperCount = institutionTr.findElement(By.xpath("td[3]")).getText().trim();
                                faculty = institutionTr.findElement(By.xpath("td[4]")).getText().trim();
                            } catch (NoSuchElementException e) {
                                try {
                                    List<WebElement> facultyList = institutionTr.findElements(By.xpath("td//small"));
                                    if (facultyList.size() > 0) {
                                        if("Faculty".equals(facultyList.get(0).getText().trim())) {
                                            continue;
                                        }
                                    }
                                    if (3 != facultyList.size()) {
                                        continue;
                                    }
                                    name = facultyList.get(0).findElement(By.xpath("a[1]")).getText();
                                    pubs = facultyList.get(1).findElement(By.xpath("a[1]")).getText();
                                    adj = facultyList.get(2).getText();
//                                    for (WebElement facultyElement : facultyList) {
//                                        try {
//                                            List<WebElement> nameCountElements = facultyElement.findElements(By.xpath("a[0]"));
//                                            name = facultyElement.findElement(By.xpath("a[1]")).getText();
//                                            name = nameCountElements.get(0).getText();
//                                            pubs = nameCountElements.get(1).getText();
//                                        } catch (NoSuchElementException e1) {
//                                            adj = facultyElement.getText();
//                                        }
//                                    }

                                } catch (NoSuchElementException e1) {
                                    log.error("no such element", e1);
                                    continue;
                                }
                            }
                            // 没有研究院姓名记录不完整继续循环
                            if (StringUtils.isBlank(name)) {
                                continue;
                            }
//                            if ( rank >= 37) {

                                CSRanking ranking = CSRanking.builder()
                                        .area(area)
                                        .range(range)
                                        .domain(domain)
                                        .direction(direction)
                                        .subDirection(subDirection)
                                        .rank(rank)
                                        .institution(institution)
                                        .paperCount(paperCount)
                                        .faculty(faculty)
                                        .name(name)
                                        .pubs(pubs)
                                        .adj(adj)
                                        .ctime(now)
                                        .build();
                                rankings.add(ranking);
//                            }
                        }
                        // 取消checkbox点击
                        checkbox.click();
                    }
                    // 点击取消对应图形
                    triangle.click();
                }
                i++;
                if (!rankings.isEmpty()) {
                    csRankingDao.batchInsert(rankings);
                    rankings.clear();
                }
            }

    }

    private void doCSRankingCrawl(String range, WebDriver webDriver) {
        Select areaSelect = new Select(webDriver.findElement(By.id("regions")));
        List<WebElement> areas = areaSelect.getOptions();
        areaList = Lists.newArrayList();
        for (WebElement element : areas) {

            String area = element.getText();
            if ("1970-1980".equals(range)
                    && ("USA and Canada".equals(area) || "South America".equals(area)
                        || "the USA".equals(area) || "Canada".equals(area))) {
                areaList.add(area);
                continue;
            }
            long now = System.currentTimeMillis();
            try {
                element.click();
            } catch (StaleElementReferenceException e200) {
                Select areaSelect2 = new Select(webDriver.findElement(By.id("regions")));
                List<WebElement> areas2 = areaSelect2.getOptions();
                for (WebElement element1: areas2) {
                    if (area.contains(element1.getText())) {
                        continue;
                    }
                    element1.click();
                    break;
                }
            }
            areaList.add(area);
            List<CSRanking> rankings = Lists.newArrayList();
            List<WebElement> elements = webDriver.findElements(By.xpath("//th[@colspan]/p[@class='text-muted']"));
            List<WebElement> directions = webDriver.findElement(By.cssSelector("table.table-striped")).findElements(By.xpath("*"));
            directions = buildTbodyElement(directions, webDriver);
            int i = 0;
//            elements = elements.subList(1, elements.size());
            for (WebElement domainElement : elements) {
                String domain = domainElement.getText();
                if (!StringUtils.isBlank(domain)) {
                    domain = domain.split(" ")[0];
                }
                log.info("range : {}, location: {}, domain : {}", range, area, domain);
                List<WebElement> trList = directions.get(i).findElements(By.tagName("tr"));
                for (WebElement tr : trList) {
                    String contentTr = tr.getText();
                    if (StringUtils.isBlank(contentTr)) {
                        continue;
                    }
                    String direction = "";
                    try {
                        direction = tr.findElement(By.tagName("label")).getText();
                    } catch (NoSuchElementException e) {
                        direction = tr.findElement(By.tagName("td")).getText();
                        if (!StringUtils.isBlank(direction)) {
                            direction = direction.substring(1);
                        }
                    }
                    direction = direction.trim();
                    WebElement triangle = tr.findElement(By.className("hovertip"));
                    List<WebElement> subTrs = tr.findElements(By.cssSelector("div.table-responsive table.table-sm tbody tr td table tbody tr"));
                    triangle.click();
                    if (subTrs.isEmpty()) {
                        triangle.click();
                        continue;
                    }
                    subTrs = subTrs.subList(1, subTrs.size());
                    for (WebElement subTr : subTrs) {
                        String subDirection;
                        try {
                            subDirection = subTr.findElement(By.tagName("a")).getText();
                        } catch (NoSuchElementException e) {
                            log.info("-------");
                            continue;
                        }
                        WebElement checkbox = subTr.findElement(By.tagName("input"));
                        checkbox.click();
                        List<WebElement> institutionTrs = webDriver.findElements(By.xpath("//table[@id='ranking']/tbody//tr"));
                        if (institutionTrs.isEmpty()) {
                            checkbox.click();
                            continue;
                        }
                        int rank = 0;
                        String institution = "";
                        String paperCount = "";
                        String faculty = "";
                        String name = "";
                        String pubs = "";
                        String adj = "";
                        for (WebElement institutionTr : institutionTrs) {
                            List<WebElement> tdList = institutionTr.findElements(By.xpath("td"));
                            if (4 == tdList.size()) {
                                try {
                                    WebElement element1 = tdList.get(1).findElement(By.tagName("small"));
                                } catch (NoSuchElementException e) {
                                    rank = 0;
                                    institution = "";
                                    paperCount = "";
                                    faculty = "";
                                    name = "";
                                    pubs = "";
                                    adj = "";
                                }

                            }
                            String rankStr = institutionTr.findElement(By.xpath("td[1]")).getText().trim();
                            if (!StringUtils.isBlank(rankStr)) {
                                try {
                                    rank = Integer
                                            .parseInt(rankStr);
                                } catch (NumberFormatException e) {

                                }
                            }
                            try {
                                if (StringUtils.isBlank(institution)) {
                                    institution = institutionTr.findElement(By.xpath("td[2]")).getText().trim();
                                    institution = institution.substring(1);
                                }
                                WebElement triangle2 = institutionTr.findElement(By.xpath("td/span[@class='hovertip']"));
                                triangle2.click();
                                paperCount = institutionTr.findElement(By.xpath("td[3]")).getText().trim();
                                faculty = institutionTr.findElement(By.xpath("td[4]")).getText().trim();
                            } catch (NoSuchElementException e) {
                                try {
                                    List<WebElement> facultyList = institutionTr.findElements(By.xpath("td//small"));
                                    if (facultyList.size() > 0) {
                                        if("Faculty".equals(facultyList.get(0).getText().trim())) {
                                            continue;
                                        }
                                    }
                                    if (3 != facultyList.size()) {
                                        continue;
                                    }
                                    name = facultyList.get(0).findElement(By.xpath("a[1]")).getText();
                                    pubs = facultyList.get(1).findElement(By.xpath("a[1]")).getText();
                                    adj = facultyList.get(2).getText();
//                                    for (WebElement facultyElement : facultyList) {
//                                        try {
//                                            List<WebElement> nameCountElements = facultyElement.findElements(By.xpath("a[0]"));
//                                            name = facultyElement.findElement(By.xpath("a[1]")).getText();
//                                            name = nameCountElements.get(0).getText();
//                                            pubs = nameCountElements.get(1).getText();
//                                        } catch (NoSuchElementException e1) {
//                                            adj = facultyElement.getText();
//                                        }
//                                    }

                                } catch (NoSuchElementException e1) {
                                    continue;
                                }
                            }
                            // 没有研究院姓名记录不完整继续循环
                            if (StringUtils.isBlank(name)) {
                                continue;
                            }
                            CSRanking ranking = CSRanking.builder()
                                    .area(area)
                                    .range(range)
                                    .domain(domain)
                                    .direction(direction)
                                    .subDirection(subDirection)
                                    .rank(rank)
                                    .institution(institution)
                                    .paperCount(paperCount)
                                    .faculty(faculty)
                                    .name(name)
                                    .pubs(pubs)
                                    .adj(adj)
                                    .ctime(now)
                                    .build();
                            rankings.add(ranking);
                        }
                        // 取消checkbox点击
                        checkbox.click();
                    }
                    // 点击取消对应图形
                    triangle.click();
                }
                i++;
                if (!rankings.isEmpty()) {
                    csRankingDao.batchInsert(rankings);
                    rankings.clear();
                }
            }
        }

    }

    private void doCSRankingCrawl() throws InterruptedException {
        List<String> urls = Lists.newArrayList(
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&canada",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&northamerica",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&southamerica",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&asia",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&australasia",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&europe",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&africa",
//                "http://csrankings.org/#/fromyear/1970/toyear/1980/index?none&world"

//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&canada",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&northamerica",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&southamerica",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&asia",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&australasia",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&europe",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&africa",
//                "http://csrankings.org/#/fromyear/1980/toyear/1990/index?none&world"

//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none"
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&canada"
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&northamerica"

//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&southamerica",
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&asia"
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&australasia"
//
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&europe"
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&africa"
//                "http://csrankings.org/#/fromyear/1990/toyear/2000/index?none&world"
//
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&canada"
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&northamerica",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&southamerica",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&asia",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&australasia",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&europe"
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&africa",
//                "http://csrankings.org/#/fromyear/2000/toyear/2010/index?none&world"
//
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none",
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&canada",
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&northamerica",
                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&southamerica"
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&asia"
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&australasia"
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&europe",
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&africa",
//                "http://csrankings.org/#/fromyear/2010/toyear/2020/index?none&world"
                );

        for (String url : urls) {
            doCSRankingInner(url);
        }

    }

}
