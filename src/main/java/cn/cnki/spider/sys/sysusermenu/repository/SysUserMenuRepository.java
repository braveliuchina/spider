package cn.cnki.spider.sys.sysusermenu.repository;

import cn.cnki.spider.sys.sysusermenu.pojo.SysUserMenu;
import cn.cnki.spider.common.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserMenuRepository extends CommonRepository<SysUserMenu, String> {
    List<SysUserMenu> findByUserId(String userId);
}
