package cn.cnki.spider.entity;

import lombok.Data;

import java.util.List;

@Data
public class AjaxArticleContent {

	private String id;

	private String verid;

	private String leadtitle;

	private String title;

	private String title1;

	private String author;

	private String content;

	private String images;

	private String imagn;

	private String setDate;

	private List<String> image;

	private List<String> imagen;

	private String title_st;
}