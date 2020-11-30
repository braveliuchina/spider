package cn.cnki.spider.entity;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@FieldNameConstants
@Document(collection = "crawl_company")
@CompoundIndexes(
        {@CompoundIndex(name = "idx_uid_companyName_exits", def = "{'uid': 1, 'companyName': 1, 'exists': 1}", background = true),
                @CompoundIndex(name = "idx_unique_uid", def = "{'uid': 1}", unique = true, background = true)})
public class BaiduBaikeSpiderItem {

    @Id
    private String id;

    private String url;

    private String uid;

    private String companyName;

    private String html;

    // 是否存在 默认为true
    private boolean exists = true;

    private long ctime;

    private long utime;

}
