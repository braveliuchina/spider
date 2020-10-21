package cn.cnki.spider.pipeline;

import cn.cnki.spider.dao.ReddotDao;
import cn.cnki.spider.entity.ReddotItem;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.Set;

@Data
@Component
@RequiredArgsConstructor
public class ReddotDBItemBatchPageModelPipeline implements Pipeline {

    private final ReddotDao reddotDao;

    private Set<ReddotItem> itemsAll = Sets.newHashSet();

    @Override
    public void process(ResultItems resultItemMap, Task task) {

        Object object = resultItemMap.get("items");
        if (null == object) {
            return;
        }
        ReddotItem items = (ReddotItem) object;
        itemsAll.add(items);
        if (itemsAll.size() >= 300) {
            reddotDao.itemBatchInsert(Lists.newArrayList(itemsAll));
            itemsAll = Sets.newHashSet();
        }
    }

}
