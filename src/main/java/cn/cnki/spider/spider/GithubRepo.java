package cn.cnki.spider.spider;

import lombok.Data;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.OOSpider;
import us.codecraft.webmagic.model.annotation.ExtractBy;
import us.codecraft.webmagic.model.annotation.TargetUrl;
import us.codecraft.webmagic.pipeline.JsonFilePageModelPipeline;

@Data
@TargetUrl("http://www.thtf.com.cn/")
//@HelpUrl("http://192.168.50.248:7777/redmine/users")
public class GithubRepo {

//    @ExtractBy(value = "//h2/text()", notNull = true)
//    private String name;

//    @ExtractByUrl("http://192.168.50.248:7777/redmine/users/(\\w+)")
//    private String author;
//
//    @ExtractBy("//div[@class='splitcontentleft']/ul/li/a/text()")
//    private String task;
	
	@ExtractBy(value = "//div[@class='phone']/text()", notNull = true)
	private String phone;

    public static void main(String[] args) {
        OOSpider.create(Site.me().setSleepTime(1000)
                , new JsonFilePageModelPipeline("C:"), GithubRepo.class)
                .addUrl("http://www.thtf.com.cn/").thread(5).run();
    }
}