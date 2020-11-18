package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.RankingDao;
import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.RankingItem;
import cn.cnki.spider.entity.ReddotItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.Set;

@Data
@Component
@RequiredArgsConstructor
public class RankingDBItemBatchPageModelPipeline implements Pipeline {

    private final RankingDao rankingDao;

    private Set<RankingItem> itemsAll = Sets.newHashSet();

    @Override
    public void process(ResultItems resultItemMap, Task task) {

        Object object = resultItemMap.get("items");
        if (null == object) {
            return;
        }
        List<RankingItem> items = (List<RankingItem>) object;
        rankingDao.itemBatchInsert(items);

    }

}
