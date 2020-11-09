package cn.cnki.spider.common.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
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
