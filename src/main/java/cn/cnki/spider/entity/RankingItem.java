package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingItem {
	
	private long id;
	
	private String rank;

	private String institution;

	private String score;

	private String subject;

	private String year;

}
