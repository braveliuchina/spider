package cn.cnki.spider.entity;

import lombok.Data;

@Data
public class SpiderConfig {
	
	private long id;
	
	private String name;
	
	private String site;
	
	private String content;
	
	private String type;
	
	private long ctime;
	
	private long utime;

	private String url;
	
}
