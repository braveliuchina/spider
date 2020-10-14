package cn.cnki.spider.sys.sysuser.service;

import cn.cnki.spider.sys.sysuser.pojo.SysUser;
import cn.cnki.spider.sys.sysuser.vo.SysUserVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public interface SysUserService extends CommonService<SysUserVo, SysUser, String> {
    Result<SysUserVo> findByLoginName(String username);
    Result<SysUserVo> resetPassword(String userId);
    PersistentTokenRepository getPersistentTokenRepository2();
}
