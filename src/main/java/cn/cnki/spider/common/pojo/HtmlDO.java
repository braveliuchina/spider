package cn.cnki.spider.common.pojo;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "crawl_html")
@CompoundIndex(name = "idx_url", def = "{'url': 1}",
        unique = true, background = true)
public class HtmlDO {

    @Id
    private String id;

    private String url;

    private String html;

    private long ctime;

    private long utime;

}
