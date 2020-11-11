package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends CommonRepository<Template, Long> {

}