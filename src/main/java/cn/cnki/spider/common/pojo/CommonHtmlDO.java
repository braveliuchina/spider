package cn.cnki.spider.common.pojo;

import cn.cnki.spider.util.CommonManagerProperty;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@FieldNameConstants
@Document(collection = "crawl_common")
@CompoundIndex(name = "idx_jobId_type", def = "{'type': 1, 'jobId': 1}", background = true)
public class CommonHtmlDO extends CommonManagerProperty {

    @Id
    private String id;

    private String type;

    private long jobId;

    // 执行记录Id
    private String hisId;

    private List<JSONObject> content;

    private String err;

    private long ctime;

    private long utime;

}
