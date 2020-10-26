package cn.cnki.spider.common.pojo;

import lombok.Data;

import java.util.List;

/**
 * 爬虫入口url
 */
@Data
public class HtmlVo {

    private String url;

    private List<String> xpathList;

}
