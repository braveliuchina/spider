package cn.cnki.spider.entity;

import lombok.Data;

import java.util.Map;

@Data
public class NestedContent {
	
	private String type;
	
	private Content list;
	
	private Map<String, Content> detail;
}
