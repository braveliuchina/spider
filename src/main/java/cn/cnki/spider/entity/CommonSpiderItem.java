package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonSpiderItem {
	
	private long id;
	
	private long xpathId;
	
	private int temp;

	private String result;

	private long ctime;

	private long utime;
	
}
