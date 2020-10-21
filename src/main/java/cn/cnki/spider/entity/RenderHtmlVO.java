package cn.cnki.spider.entity;

import lombok.Data;

@Data
public class RenderHtmlVO {

    private int crawlType;

    private String inputUrl;

    private String prefix;

}
