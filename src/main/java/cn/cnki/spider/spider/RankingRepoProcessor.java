package cn.cnki.spider.spider;

import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.ReddotUrl;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;

@Data
//@Component
public class RankingRepoProcessor implements PageProcessor {

    private String domain;

    private Site site;

    private final TypeReference typeReference = new TypeReference<HashMap<String, Content>>() {
    };

    @Override
    public Site getSite() {

        site = Site.me().setRetryTimes(3)
                .setSleepTime(5);
        return site;

    }

    @Override
    public void process(Page page) {

        List<String> urls = page.getHtml().xpath("//div[@class='select-subj']/ul/li/a/@href").all();

        long now = System.currentTimeMillis();
        List<ReddotUrl> reddotUrls = Lists.newArrayList();
        urls.forEach(url -> {
            ReddotUrl urlObj = ReddotUrl.builder()
                    .url(url)
                    .type(domain)
                    .done(0)
                    .ctime(now)
                    .utime(now)
                    .build();
            reddotUrls.add(urlObj);
        });

        page.putField("urls", reddotUrls);
    }
}