package cn.cnki.spider.sys.syssetting.service;

import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.sys.syssetting.pojo.SysSetting;
import cn.cnki.spider.sys.syssetting.repository.SysSettingRepository;
import cn.cnki.spider.sys.syssetting.vo.SysSettingVo;
import cn.cnki.spider.util.SysSettingUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@Transactional
public class SysSettingServiceImpl extends CommonServiceImpl<SysSettingVo, SysSetting, String> implements SysSettingService {

    @PersistenceContext
    private EntityManager em;

    private final SysSettingRepository sysSettingRepository;

    public SysSettingServiceImpl(SysSettingRepository sysSettingRepository,
                                 CommonRepository<SysSetting, String> commonRepository) {
        super(commonRepository);
        this.sysSettingRepository = sysSettingRepository;
    }

    @Override
    public Result<SysSettingVo> save(SysSettingVo entityVo) {
        //调用父类
        Result<SysSettingVo> result = super.save(entityVo);

        //更新系统设置时同步更新公用静态集合sysSettingMap
        SysSettingUtil.setSysSettingMap(result.getData());

        return result;
    }
}
