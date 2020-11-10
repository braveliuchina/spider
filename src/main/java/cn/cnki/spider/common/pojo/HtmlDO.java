package cn.cnki.spider.common.pojo;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@FieldNameConstants
@Document(collection = "crawl_html")
@CompoundIndex(name = "idx_jobId_type", def = "{'jobId': 1, 'type': 1}",background = true)
public class HtmlDO {

    @Id
    private String id;

    private String url;

    private long jobId;

    private String type;

    private String html;

    private long ctime;

    private long utime;

}
