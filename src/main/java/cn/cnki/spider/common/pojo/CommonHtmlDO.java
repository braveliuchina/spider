package cn.cnki.spider.common.pojo;

import cn.cnki.spider.util.CommonManagerProperty;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
public class CommonHtmlDO extends CommonManagerProperty {

    @Id
    private String id;

    private String type;

    private long jobId;

    private List<JSONObject> content;

    private long ctime;

    private long utime;

}
