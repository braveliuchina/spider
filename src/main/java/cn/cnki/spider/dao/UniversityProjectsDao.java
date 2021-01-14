package cn.cnki.spider.dao;

import cn.cnki.spider.entity.UniversityProjects;

import java.util.List;

public interface UniversityProjectsDao {

	public int batchInsert(List<UniversityProjects> records);
	
}
