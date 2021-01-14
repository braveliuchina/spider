package cn.cnki.spider.common.controller;

import cn.cnki.spider.dao.UniversityProjectsDao;
import cn.cnki.spider.entity.UniversityProjects;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
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

    private final UniversityProjectsDao universityProjectsDao;

    @Bean
    public void crawlItem() throws UnsupportedEncodingException, InterruptedException {
        WebDriver webDriver = doUniversityProjects("2019", 21);
        doCrawl(webDriver, "2019");

//        WebDriver webDriver2 = doUniversityProjects("2020");
//        doCrawl(webDriver2, "2020");
    }

    private void doCrawl(WebDriver webDriver, String year) throws InterruptedException {
        int size = 30;
        do {
            WebElement pageSpan = webDriver.findElement(By.xpath("//span[@class='active']"));
            String pageNo = pageSpan.getText();
            Integer page = Integer.parseInt(pageNo);
            List<WebElement> elements = webDriver.findElements(By.className("c--tr"));
            size = elements.size();
            List<UniversityProjects> projectList = Lists.newArrayList();
            for (WebElement element : elements) {

                String code = "";
                String name = "";
                String person = "";
                String level = "";
                String teacher = "";
                String university = "";
                String a = "";
                String type = "";
                String timeRange = "";
                String subject = "";
                String category = "";
                String planDate = "";
                String personInfo = "";
                String teacherUnit = "";
                String teacherDetail = "";
                String teacherType = "";

                List<WebElement> tds = element.findElements(By.tagName("td"));
                if (tds.isEmpty()) {
                    continue;
                }
                tds = tds.subList(1, tds.size());
                if (tds.size() != 7) {
                    continue;
                }
                UniversityProjects projects = new UniversityProjects();
                for (int i = 0; i < tds.size(); i++) {
                    if (0 == i) {
                        code = tds.get(0).getText().trim();
                    }
                    if (1 == i) {
                        name = tds.get(1).getText().trim();
                    }
                    if (2 == i) {
                        person = tds.get(2).getText().trim();
                    }
                    if (3 == i) {
                        level = tds.get(3).getText().trim();
                    }
                    if (4 == i) {
                        teacher = tds.get(4).getText().trim();
                    }
                    if (5 == i) {
                        university = tds.get(5).getText().trim();
                    }
                    if (6 == i) {
                        WebElement aEle = tds.get(6).findElement(By.tagName("a"));
                        a = aEle.getAttribute("href");
                        break;
                    }
                }
                projects.setCode(code);
                projects.setName(name);
                projects.setPerson(person);
                projects.setLevel(level);
                projects.setTeacher(teacher);
                projects.setUniversity(university);
                projects.setA(a);
                projects.setPage(page);
                projects.setYear(year);
                projectList.add(projects);
            }
            universityProjectsDao.batchInsert(projectList);
            try {
                WebElement nextPage = webDriver.findElement(By.linkText("下一页"));
                nextPage.click();
            } catch (ElementClickInterceptedException e) {
                log.error("重新点击");
                WebElement nextPage = webDriver.findElement(By.linkText("下一页"));
                nextPage.click();
            }
            Thread.sleep(10000L);
        } while (size == 30);
    }

    private WebDriver doUniversityProjects(String year, int page) throws InterruptedException {
        WebDriver webDriver = null;

        File file = new File("C:/spider-app/spider-app/drivers/chromedriver.exe");
        System.setProperty("webdriver.chrome.driver", file.getAbsolutePath());

        ChromeOptions options = new ChromeOptions();
        webDriver = new ChromeDriver(options);
        webDriver.manage().window().setSize(new Dimension(1300, 800));
        String url = "http://gjcxcy.bjtu.edu.cn/NewLXItemListForStudent.aspx?year=%s&IsLXItem=0&type=student&itemName=&itemCode=&fzrName=&fzrAccount=&xueXiaoName=&zdjsName=&itemType=&itemLevel=&xsubjectId=&xueXiaoCode=";
        webDriver.get(String.format(url, year));
        // 等待已确保页面正常加载
        Thread.sleep(25000L);
        WebElement element = webDriver.findElement(By.id("ctl00$ContentMain$AspNetPager1_input"));
        element.clear();
        element.sendKeys(String.valueOf(page));
        WebElement btn = webDriver.findElement(By.id("ctl00$ContentMain$AspNetPager1_btn"));
        btn.click();
        Thread.sleep(10000L);
        return webDriver;
    }

}
