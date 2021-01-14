package cn.cnki.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityProjects {
	
	private long id;
	
	private String code;

	private String name;

	private String person;

	private String level;

	private String teacher;

	private String university;

	private String type;

	private String timeRange;

	private String subject;

	private String category;

	private String planDate;

	private String personInfo;

	private String teacherInfo;

	private String a;

	private Integer page;

	private String year;

	private Long ctime;
	
}
