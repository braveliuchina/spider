package cn.cnki.spider.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;

/**
 * 分页条件（参考JqGrid插件）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageCondition {
    private int page = 1;//当前页码
    private int rows = 10;//页面大小
    private String sidx;//排序字段
    private String sord;//排序方式

    /**
     * 获取JPA的分页查询对象
     */
    public Pageable getPageable() {
        //处理非法页码
        if (page < 0) {
            page = 1;
        }
        //处理非法页面大小
        if (rows < 0) {
            rows = 10;
        }
        return PageRequest.of(page - 1, rows);
    }

    public PageCondition(int page, int rows) {
        super();
        this.page = page;
        this.rows = rows;
    }
}
