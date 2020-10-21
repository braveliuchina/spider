package cn.cnki.spider.spider;

import cn.cnki.spider.pipeline.TyrbJsonFilePageModelPipeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.model.OOSpider;
import us.codecraft.webmagic.model.annotation.ExtractBy;
import us.codecraft.webmagic.model.annotation.ExtractByUrl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
@ExtractBy(value = "//article", multi = true)
//@TargetUrl("http://epaper.tywbw.com/tyrb/20200826/a59bcd35be8b656136972144885e2cd6.xml")
//@TargetUrl("http://epaper.tywbw.com/tyrb/\\w+/.*.xml")
//@HelpUrl("http://epaper.tywbw.com/tyrb/\\w+/.*.cfg")
public class TyrbRepo {

	@ExtractByUrl("http://epaper.tywbw.com/tyrb/(\\w+)/.*")
	private String dateStr;

	@ExtractByUrl("http://epaper.tywbw.com/tyrb/.*/(\\w+).xml")
	private String cfgStr;

	@ExtractBy(value = "//pageinfo/@PageNo", source = ExtractBy.Source.RawHtml)
	private String pageNo;

	@ExtractBy(value = "//pageinfo/@PageName", source = ExtractBy.Source.RawHtml)
	private String pageName;

	@ExtractBy(value = "//IntroTitle/text()")
	private String introTitle;

	@ExtractBy(value = "//Title/text()")
	private String title;

	@ExtractBy(value = "//SubTitle/text()")
	private String subTitle;

	@ExtractBy(value = "//Property/text()")
	private String property;

	@ExtractBy(value = "//ZhuanBan/text()")
	private String zhuanBan;

	@ExtractBy(value = "//Author/text()")
	private String author;

	@ExtractBy(value = "//Source/text()")
	private String source;

	@ExtractBy(value = "//Content/tidyText()")
	private String content;

	@ExtractBy(value = "//ImageList/img/@Name")
	private List<String> image;

	public TyrbRepo buildTypeRepoArticle(String imgPrefix) {
		
			TyrbRepo article = new TyrbRepo(
					filterBlankSpecialChar(dateStr),
					filterBlankSpecialChar(cfgStr),
					filterBlankSpecialChar(pageNo),
					filterBlankSpecialChar(pageName),
					filterBlankSpecialChar(introTitle),
					filterBlankSpecialChar(title),
					filterBlankSpecialChar(subTitle),
					filterBlankSpecialChar(property),
					filterBlankSpecialChar(zhuanBan),
					filterBlankSpecialChar(author),
					filterBlankSpecialChar(source),
					filterBlankSpecialChar(content),
					buildImgList(image, imgPrefix));
		return article;
	}
	
	private List<String> buildImgList(List<String> imgList, String imgPrefix) {
		if (null == imgList || imgList.isEmpty()) {
			return Lists.newArrayList();
		}
		List<String> result = Lists.newArrayList();
		imgList.stream().forEach(img -> {
			result.add(imgPrefix + filterBlankSpecialChar(img));
		});
		return result;
	}

	private String filterBlankSpecialChar(String candidate) {
		if (StringUtils.isBlank(candidate)) {
			return "";
		}
		if (!candidate.contains("CDATA")) {
			return candidate.trim();
		}
		Pattern p = Pattern.compile(".*\\[CDATA\\[(.*)\\]\\].*");
		Matcher m = p.matcher(candidate);
		if(m.matches()){
			return m.group(1).trim();
		}
		return ""; 
	}

	public static void main(String[] args) {
		OOSpider.create(Site.me().setSleepTime(1000), new TyrbJsonFilePageModelPipeline("C:/spider"), TyrbRepo.class)
				.addUrl("http://epaper.tywbw.com/tyrb/20200826/04e43abb90a93b9069e0ed5275640094.xml").thread(5).run();
	}
}