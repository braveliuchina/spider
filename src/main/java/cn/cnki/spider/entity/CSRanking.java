package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CSRanking {
	
	private long id;
	
	private String area;

	private String range;

	private String domain;

	private String direction;

	private String subDirection;

	private Integer rank;

	private String institution;

	private String paperCount;

	private String faculty;

	private String name;

	private String pubs;

	private String adj;

	private long ctime;
	
}
