package cn.cnki.spider.spider;

import cn.cnki.spider.common.pojo.CommonHtmlDO;
import cn.cnki.spider.entity.Content;
import cn.cnki.spider.util.BeanAddPropertiesUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
//@Component
public class CommonRepoProcessor implements PageProcessor {

    private String domain;

    private Site site;

    private List<String> xpathList;

    private long jobId;

    private String type;

    private String hisId;

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
        CommonHtmlDO item = new CommonHtmlDO();

        try {
            if (null == xpathList || xpathList.isEmpty()) {
                return;
            }

            List<JSONObject> jsonList = Lists.newArrayList();
            Map<String, List<String>> map = Maps.newHashMap();
            for (int i = 0; i < xpathList.size(); i++) {

                String xpath = xpathList.get(i);
                List<String> field = page.getHtml().xpath(xpath).all();
                map.put("field" + (i + 1), field);

            }
            List<String> fields = map.get("field1");
            for (int i = 0; i < fields.size(); i++) {
                JSONObject json = new JSONObject();
                int j = 1;

                for (String key : map.keySet()) {

                    List<String> newList = map.get(key);
                    json.put("field" + j, newList.get(i));
                    j++;
                }
                jsonList.add(json);
            }
            item.setContent(jsonList);
        } catch (Exception e) {
            item.setErr(e.getMessage());
        }

        long now = System.currentTimeMillis();
        item.setJobId(jobId);
        item.setType(type);
        item.setCtime(now);
        item.setUtime(now);
        item.setHisId(hisId);

        page.putField("item", item);
    }
}