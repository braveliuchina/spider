package cn.cnki.spider.sys.sysshortcutmenu.controller;

import cn.cnki.spider.sys.sysshortcutmenu.service.SysShortcutMenuService;
import cn.cnki.spider.sys.sysshortcutmenu.vo.SysShortcutMenuVo;
import cn.cnki.spider.common.controller.CommonController;
import cn.cnki.spider.sys.sysshortcutmenu.pojo.SysShortcutMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/sysShortcutMenu/")
public class SysShortcutMenuController extends CommonController<SysShortcutMenuVo, SysShortcutMenu, String> {
    @Autowired
    private SysShortcutMenuService sysShortcutMenuService;
}
