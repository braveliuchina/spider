package cn.cnki.spider.user.controller;

import cn.cnki.spider.sys.sysshortcutmenu.service.SysShortcutMenuService;
import cn.cnki.spider.sys.sysshortcutmenu.vo.SysShortcutMenuVo;
import cn.cnki.spider.sys.sysuser.service.SysUserService;
import cn.cnki.spider.sys.sysuser.vo.SysUserVo;
import cn.cnki.spider.user.service.UserService;
import cn.cnki.spider.annotation.Decrypt;
import cn.cnki.spider.annotation.Encrypt;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * 登录用户访问
 */
@RestController
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private SysShortcutMenuService sysShortcutMenuService;

    @GetMapping("userinfo")
    public ModelAndView userinfo() {
        SysUserVo sysUserVo = sysUserService.findByLoginName(SecurityUtil.getLoginUser().getUsername()).getData();
        sysUserVo.setPassword(null);
        return new ModelAndView("user/userinfo", "user", sysUserVo);
    }

    @GetMapping("shortcMenu")
    public ModelAndView shortcMenu() {
        return new ModelAndView("user/shortcMenu");
    }

    /**
     * 修改密码
     */
    @PostMapping("updatePassword")
    @Decrypt
    @Encrypt
    public Result<SysUserVo> updatePassword(SysUserVo sysUserVo) {
        return userService.updatePassword(sysUserVo.getOldPassword(), sysUserVo.getNewPassword());
    }

    /**
     * 修改部分用户属性
     */
    @PostMapping("updateUser")
    @Decrypt
    @Encrypt
    public Result<SysUserVo> updateUser(SysUserVo sysUserVo) {
        return userService.updateUser(sysUserVo);
    }

    /**
     * 分层级
     */
    @PostMapping("shortcutMenuListByTier")
    @Decrypt
    @Encrypt
    public Result<List<SysShortcutMenuVo>> shortcutMenuListByTier() {
        SysUserVo sysUserVo = sysUserService.findByLoginName(SecurityUtil.getLoginUser().getUsername()).getData();
        return sysShortcutMenuService.findByUserId(sysUserVo.getUserId());
    }

    /**
     * 保存
     */
    @PostMapping("shortcutMenuSave")
    @Decrypt
    @Encrypt
    public Result<SysShortcutMenuVo> shortcutMenuSave(SysShortcutMenuVo sysShortcutMenuVo) {
        SysUserVo sysUserVo = sysUserService.findByLoginName(SecurityUtil.getLoginUser().getUsername()).getData();
        sysShortcutMenuVo.setUserId(sysUserVo.getUserId());
        return sysShortcutMenuService.save(sysShortcutMenuVo);
    }

    @DeleteMapping("shortcutMenuDelete/{id}")
    public Result<String> shortcutMenuDelete(@PathVariable("id") String id) {
        SysUserVo sysUserVo = sysUserService.findByLoginName(SecurityUtil.getLoginUser().getUsername()).getData();
        SysShortcutMenuVo sysShortcutMenuVo = new SysShortcutMenuVo();
        sysShortcutMenuVo.setUserId(sysUserVo.getUserId());
        List<SysShortcutMenuVo> sysShortcutMenuVoList = sysShortcutMenuService.list(sysShortcutMenuVo).getData();

        //判断是不是自己的便捷菜单
        boolean flag = false;
        for (SysShortcutMenuVo shortcutMenuVo : sysShortcutMenuVoList) {
            if (shortcutMenuVo.getShortcutMenuId().equals(id)) {
                flag = true;
                break;
            }
        }

        if(flag){
            return sysShortcutMenuService.delete(id);
        }else{
            return Result.of(null,false,"请不要删除别人个性菜单！");
        }
    }
}
