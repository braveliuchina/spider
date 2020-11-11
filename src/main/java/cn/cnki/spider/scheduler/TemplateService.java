package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.service.CommonService;

import java.util.List;

public interface TemplateService extends CommonService<TemplateVO, Template, Long> {

    List<TemplateVO> list();

}
