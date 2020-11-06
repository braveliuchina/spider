package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AACSB {
	
	private long id;
	
	private String university;

	private String region;

	private String academy;

	private String accreditation;

	private long ctime;

	private long utime;
	
}
