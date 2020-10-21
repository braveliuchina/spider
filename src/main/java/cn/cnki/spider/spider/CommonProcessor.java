package cn.cnki.spider.spider;

import cn.cnki.spider.entity.CommonSpiderItem;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

@Slf4j
@Data
@Component
public class CommonProcessor implements PageProcessor {

    private Site site;

    private String rule;

    private CommonSpiderItem item;

    @Override
    public Site getSite() {

        if (null != site) {
            return site;
        }
        site = Site.me().setCharset("UTF-8")
                .setRetryTimes(3).setCycleRetryTimes(3)
                .setSleepTime(5);
        return site;

    }

    @Override
    public void process(Page page) {

        List<String> resultList = page.getHtml().xpath("//ul[@class='conMItemList']/li//text()").all();

        List<CommonSpiderItem> list = Lists.newArrayList();

        long now = System.currentTimeMillis();
        for (String result: resultList) {
            CommonSpiderItem commonSpiderItem = new CommonSpiderItem();
            commonSpiderItem.setXpathId(item.getXpathId());
            commonSpiderItem.setTemp(1);
            commonSpiderItem.setResult(result);
            commonSpiderItem.setCtime(now);
            commonSpiderItem.setUtime(now);
            list.add(commonSpiderItem);
        }

        page.putField("resultList", list);
    }

}