package cn.cnki.spider.spider;

import cn.cnki.spider.entity.BaiduBaikeSpiderItem;
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
public class BaiduBaikeProcessor implements PageProcessor {

    private Site site;

    private String uid;

    private String companyName;

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
        BaiduBaikeSpiderItem item = new BaiduBaikeSpiderItem();
        item.setUid(uid);
        item.setCompanyName(companyName);
        long now = System.currentTimeMillis();
        item.setCtime(now);
        item.setUtime(now);
        String url = page.getUrl().toString();
        item.setUrl(url);
        String html = page.getHtml().toString();
        //
        if (html.contains("百度百科错误页")) {
            item.setExists(false);
        }

        item.setHtml(html);

        page.putField("result", item);
    }

}