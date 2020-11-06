package cn.cnki.spider.sys.sysuserauthority.service;

import cn.cnki.spider.sys.sysuserauthority.pojo.SysUserAuthority;
import cn.cnki.spider.sys.sysuserauthority.vo.SysUserAuthorityVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;

import java.util.List;

public interface SysUserAuthorityService extends CommonService<SysUserAuthorityVo, SysUserAuthority, String> {
    Result<List<SysUserAuthorityVo>> findByUserId(String userId);

    Result<Boolean> saveAllByUserId(String userId, String authorityIdList);
}
