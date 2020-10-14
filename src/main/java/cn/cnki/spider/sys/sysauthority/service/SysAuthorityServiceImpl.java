package cn.cnki.spider.sys.sysauthority.service;

import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.config.security.MyFilterInvocationSecurityMetadataSource;
import cn.cnki.spider.sys.sysauthority.pojo.SysAuthority;
import cn.cnki.spider.sys.sysauthority.repository.SysAuthorityRepository;
import cn.cnki.spider.sys.sysauthority.vo.SysAuthorityVo;
import cn.cnki.spider.sys.sysuserauthority.service.SysUserAuthorityService;
import cn.cnki.spider.sys.sysuserauthority.vo.SysUserAuthorityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class SysAuthorityServiceImpl extends CommonServiceImpl<SysAuthorityVo, SysAuthority, String> implements SysAuthorityService{

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private SysAuthorityRepository sysAuthorityRepository;

    @Autowired
    private SysAuthorityService sysAuthorityService;

    @Autowired
    private SysUserAuthorityService sysUserAuthorityService;

    @Autowired
    private MyFilterInvocationSecurityMetadataSource myFilterInvocationSecurityMetadataSource;

    /**
     * 重写save方法，当新增、修改权限表后需要去更新权限集合
     */
    @Override
    public Result<SysAuthorityVo> save(SysAuthorityVo entityVo) {
        Result<SysAuthorityVo> result = super.save(entityVo);

        //更新权限集合
        List<SysAuthorityVo> authorityVoList = sysAuthorityService.list(new SysAuthorityVo()).getData();
        myFilterInvocationSecurityMetadataSource.setRequestMap(authorityVoList);
        return result;
    }

    /**
     * 重写delete方法
     */
    @Override
    public Result<String> delete(String id) {
        //删除权限之前，删除用户权限关联表对应数据
        SysUserAuthorityVo sysUserAuthorityVo = new SysUserAuthorityVo();
        sysUserAuthorityVo.setAuthorityId(id);
        sysUserAuthorityService.list(sysUserAuthorityVo).getData().forEach((vo)->{
            sysUserAuthorityService.delete(vo.getUserAuthorityId());
        });

        //再删除自己
        Result<String> result = super.delete(id);

        //更新权限集合
        List<SysAuthorityVo> authorityVoList = sysAuthorityService.list(new SysAuthorityVo()).getData();
        myFilterInvocationSecurityMetadataSource.setRequestMap(authorityVoList);
        return result;
    }
}
