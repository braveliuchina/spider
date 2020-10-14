package cn.cnki.spider.sys.sysusermenu.service;

import cn.cnki.spider.sys.sysmenu.vo.SysMenuVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import cn.cnki.spider.sys.sysusermenu.pojo.SysUserMenu;
import cn.cnki.spider.sys.sysusermenu.vo.SysUserMenuVo;

import java.util.List;

public interface SysUserMenuService extends CommonService<SysUserMenuVo, SysUserMenu, String> {
    Result<List<SysMenuVo>> findByUserId(String userId);

    Result<Boolean> saveAllByUserId(String userId, String menuIdList);
}
