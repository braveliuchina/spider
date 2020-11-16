package cn.cnki.spider.common.pojo;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@FieldNameConstants
@Document(collection = "crawl_newspaper_article")
@CompoundIndex(name = "idx_jobId_hisId", def = "{'jobId': 1, 'hisId': 1}",background = true)
public class ArticleDO {

    @Id
    private String id;

    private Long jobId;

    private String hisId;

    private Long templateId;

    private String date;

    private String cfgId;

    private String pageNo;

    private String pageName;

    private String introTitle;

    private String author;

    private String title;

    private String time;

    private String subTitle;

    private String source;

    private String zhuanBan;

    private String property;

    private String content;

    private String contentAll;

    private String prefix;

    private String image;

    private String jpg;

    private String pdf;

    private String articleNo;

    private long ctime;

    private long utime;

}
