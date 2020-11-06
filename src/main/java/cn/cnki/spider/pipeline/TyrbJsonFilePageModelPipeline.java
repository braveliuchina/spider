package cn.cnki.spider.pipeline;

import cn.cnki.spider.spider.TyrbRepo;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.JsonFilePageModelPipeline;

public class TyrbJsonFilePageModelPipeline extends JsonFilePageModelPipeline {
	
	private final static String IMG_PREFIX = "http://epaper.tywbw.com/tyrb/%s/";

	public TyrbJsonFilePageModelPipeline(String path) {
		super(path);
	}
	
	@Override
	public void process(Object o, Task task) {

		if (o instanceof TyrbRepo) {
			String imgPrefix = String.format(IMG_PREFIX, ((TyrbRepo)o).getDateStr());
			o = ((TyrbRepo)o).buildTypeRepoArticle(imgPrefix);
			super.process(o, task);
		}
		
	}

}
