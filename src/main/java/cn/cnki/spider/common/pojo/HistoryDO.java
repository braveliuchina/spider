package cn.cnki.spider.common.pojo;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@FieldNameConstants
@Document(collection = "crawl_history")
@CompoundIndex(name = "idx_jobId", def = "{'jobId': 1}",background = true)
public class HistoryDO {

    @Id
    private String id;

    private Long jobId;

    // 异常信息
    private String err;

    // 0 停止状态 执行失败 1 本次运行中 2 本次执行成功
    private Integer status;

    private Long ctime;

    private Long utime;

}
