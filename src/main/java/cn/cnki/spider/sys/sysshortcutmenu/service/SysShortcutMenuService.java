package cn.cnki.spider.sys.sysshortcutmenu.service;

import cn.cnki.spider.sys.sysshortcutmenu.vo.SysShortcutMenuVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import cn.cnki.spider.sys.sysshortcutmenu.pojo.SysShortcutMenu;

import java.util.List;

public interface SysShortcutMenuService extends CommonService<SysShortcutMenuVo, SysShortcutMenu, String> {
    Result<List<SysShortcutMenuVo>> findByUserId(String userId);
}
