package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageInfo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.util.CommonForkJoinPool;
import cn.cnki.spider.util.SpringUtil;
import cn.cnki.spider.util.SqlUtil;
import com.alibaba.fastjson.JSONArray;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class TemplateServiceImpl
        extends CommonServiceImpl<TemplateVO, Template, Long> implements TemplateService {

    @PersistenceContext
    private EntityManager em;

    private final TemplateRepository templateRepository;

    private final CommonForkJoinPool forkJoinPool = new CommonForkJoinPool(16, "crawlPool");

    public TemplateServiceImpl(TemplateRepository templateRepository,
                               CommonRepository<Template, Long> commonRepository) {
        super(commonRepository);
        this.templateRepository = templateRepository;
    }

    @Override
    public Result<PageInfo<TemplateVO>> page(TemplateVO entityVo) {
        //根据实体、Vo直接拼接全部SQL
        StringBuilder sql = SqlUtil.joinSqlByEntityAndVo(Template.class,entityVo);

        //设置SQL、映射实体，以及设置值，返回一个Query对象
        Query query = em.createNativeQuery(sql.toString(), Template.class);

        //分页设置，page从0开始
        PageRequest pageRequest = PageRequest.of(entityVo.getPage() - 1, entityVo.getRows());

        //获取最终分页结果
        Result<PageInfo<TemplateVO>> result = Result
                .of(PageInfo.of(PageInfo.getJPAPage(query,pageRequest,em), TemplateVO.class));

        return result;
    }

    @Override
    public List<TemplateVO> list() {
        TemplateVO job = new TemplateVO();
        Result<List<TemplateVO>> listResult = this.list(job);
        return listResult.getData();
    }

}