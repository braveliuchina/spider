package cn.cnki.spider.common.service;

import java.util.List;

public interface AbstractCrawlService {
	
	public void crawl() throws Exception;
	
	public void crawl(List<String> urls, int thread) throws Exception;

	public void crawlByDate(String year, String month, String day) throws Exception;
	
}
