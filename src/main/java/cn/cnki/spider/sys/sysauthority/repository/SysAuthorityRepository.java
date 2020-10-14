package cn.cnki.spider.sys.sysauthority.repository;

import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.sys.sysauthority.pojo.SysAuthority;
import org.springframework.stereotype.Repository;

@Repository
public interface SysAuthorityRepository extends CommonRepository<SysAuthority, String> {
}
