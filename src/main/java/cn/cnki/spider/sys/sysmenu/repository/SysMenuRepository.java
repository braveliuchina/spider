package cn.cnki.spider.sys.sysmenu.repository;

import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.sys.sysmenu.pojo.SysMenu;
import org.springframework.stereotype.Repository;

@Repository
public interface SysMenuRepository extends CommonRepository<SysMenu, String> {
}
