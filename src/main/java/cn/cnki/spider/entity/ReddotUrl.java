package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReddotUrl {
	
	private long id;
	
	private String type;

	private String url;

	private int done;

	private long ctime;

	private long utime;
	
}
