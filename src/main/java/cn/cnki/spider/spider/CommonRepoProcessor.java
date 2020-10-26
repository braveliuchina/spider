package cn.cnki.spider.spider;

import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.ReddotItem;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;

@Data
@Component
public class CommonRepoProcessor implements PageProcessor {

    private String domain;

    private Site site;

    private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>() {
    };

    @Override
    public Site getSite() {

        site = Site.me().setRetryTimes(3)
                .setSleepTime(4);
        return site;

    }

    @Override
    public void process(Page page) {
        ReddotItem item = new ReddotItem();
        try {
            String itemType = page.getHtml().xpath("//div[@class='row']/div/span[@class='subtitle']/text()").toString();

            String itemName = page.getHtml().xpath("//div[@class='row']/div/h1[@class='h2']/text()").toString();

            String img = page.getHtml().xpath("//div[@class='row']/div/p[@class='awards']/img/@src").toString();

            String manufacturer = page.getHtml().xpath("//div[@class='credits']/ul//li[1]//div[@class='value']/text()").toString();

            String label = page.getHtml().xpath("//div[@class='credits']/ul//li[2]/div[@class='label']/text()").toString();

            String inHouseDesignOrDesign = page.getHtml().xpath("//div[@class='credits']/ul//li[2]/div[@class='value']/text()").toString();

            String year = img.substring(img.lastIndexOf("/") + 1).replaceAll("(.*)_(\\d\\d\\d\\d)_(.*)", "$2");

            item.setDomain(domain);
            item.setItemType(itemType);
            item.setItemName(itemName);
            item.setManufacturer(manufacturer);
            item.setAwardYear(year);
            item.setAwardType(img);

            item.setUrl(page.getUrl().toString());

            if (!StringUtils.isBlank(label)) {
                if (label.contains("house")) {
                    item.setInHouseDesign(inHouseDesignOrDesign);
                } else {
                    item.setDesign(inHouseDesignOrDesign);
                }
            } else {
                item.setDesign(inHouseDesignOrDesign);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();
        item.setCtime(now);
        item.setUtime(now);

        page.putField("items", item);
    }
}