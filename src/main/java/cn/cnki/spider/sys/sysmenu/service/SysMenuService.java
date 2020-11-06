package cn.cnki.spider.sys.sysmenu.service;

import cn.cnki.spider.common.service.CommonService;
import cn.cnki.spider.sys.sysmenu.pojo.SysMenu;
import cn.cnki.spider.sys.sysmenu.vo.SysMenuVo;
import cn.cnki.spider.common.pojo.Result;

import java.util.List;

public interface SysMenuService extends CommonService<SysMenuVo, SysMenu, String> {
    Result<List<SysMenuVo>> listByTier(SysMenuVo entityVo);
}
