package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpiderArticle {
	
	private long id;
	
	private long protocalId;
	
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
	
	
	private String path;
	
	private String directory;
	
}
