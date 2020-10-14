package cn.cnki.spider.user.service;

import cn.cnki.spider.sys.sysuser.vo.SysUserVo;
import cn.cnki.spider.common.pojo.Result;

public interface UserService {
    Result<SysUserVo> updatePassword(String oldPassword, String newPassword);

    Result<SysUserVo> updateUser(SysUserVo sysUserVo);
}
