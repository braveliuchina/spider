package cn.cnki.spider.sys.sysuser.repository;

import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.sys.sysuser.pojo.SysUser;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRepository extends CommonRepository<SysUser, String> {
    SysUser findByLoginName(String username);
}
