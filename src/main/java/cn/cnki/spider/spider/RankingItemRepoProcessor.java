package cn.cnki.spider.spider;

import cn.cnki.spider.entity.Content;
import cn.cnki.spider.entity.RankingItem;
import cn.cnki.spider.entity.ReddotItem;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;

@Data
@Component
public class RankingItemRepoProcessor implements PageProcessor {

    private String year;

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
        List<RankingItem> rankingItems = Lists.newArrayList();
        try {

            List<String> institution = page.getHtml().xpath("//table[@id='UniversityRanking']/tbody/tr/td[@class='left']/a/text()").all();
            List<String> rank = page.getHtml().xpath("//table[@id='UniversityRanking']/tbody/tr/td[1]/text()").all();

            List<String> score = page.getHtml().xpath("//table[@id='UniversityRanking']/tbody/tr/td[5]/text()").all();

            String subject = page.getHtml().xpath("//div[@id='breadcrumb']/ul//li[3]/text()").toString();

            for (int i = 0; i < institution.size(); i++) {
                RankingItem item = new RankingItem();
                item.setInstitution(institution.get(i));
                item.setRank(rank.get(i));
                item.setScore(score.get(i));
                item.setSubject(subject);
                item.setYear(year);
                rankingItems.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        page.putField("items", rankingItems);
    }
}