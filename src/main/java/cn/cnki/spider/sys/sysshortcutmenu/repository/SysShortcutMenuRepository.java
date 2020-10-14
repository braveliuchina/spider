package cn.cnki.spider.sys.sysshortcutmenu.repository;

import cn.cnki.spider.sys.sysshortcutmenu.pojo.SysShortcutMenu;
import cn.cnki.spider.common.repository.CommonRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysShortcutMenuRepository extends CommonRepository<SysShortcutMenu, String> {
    List<SysShortcutMenu> findByUserId(String userId);
}
