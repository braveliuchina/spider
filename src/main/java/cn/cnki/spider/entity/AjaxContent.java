package cn.cnki.spider.entity;

import lombok.Data;

import java.util.List;

@Data
public class AjaxContent {

	private List<AjaxBanContent> ban;

	private List<List<AjaxArticleContent>> article;
}