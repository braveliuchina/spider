package cn.cnki.spider.sys.syssetting.repository;

import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.sys.syssetting.pojo.SysSetting;
import org.springframework.stereotype.Repository;

@Repository
public interface SysSettingRepository extends CommonRepository<SysSetting, String> {
}
