package cn.cnki.spider.sys.sysuserauthority.repository;

import cn.cnki.spider.sys.sysuserauthority.pojo.SysUserAuthority;
import cn.cnki.spider.common.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserAuthorityRepository extends CommonRepository<SysUserAuthority, String> {
    List<SysUserAuthority> findByUserId(String userId);
}
