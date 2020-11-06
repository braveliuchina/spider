package cn.cnki.spider.sys.sysuserauthority.service;

import cn.cnki.spider.common.repository.CommonRepository;
import cn.cnki.spider.sys.sysuserauthority.pojo.SysUserAuthority;
import cn.cnki.spider.sys.sysuserauthority.repository.SysUserAuthorityRepository;
import cn.cnki.spider.sys.sysuserauthority.vo.SysUserAuthorityVo;
import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.service.CommonServiceImpl;
import cn.cnki.spider.util.CopyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class SysUserAuthorityServiceImpl extends CommonServiceImpl<SysUserAuthorityVo, SysUserAuthority, String> implements SysUserAuthorityService{

    @PersistenceContext
    private EntityManager em;

    private final SysUserAuthorityRepository sysUserAuthorityRepository;

    public SysUserAuthorityServiceImpl(CommonRepository<SysUserAuthority, String> commonRepository,
                                       SysUserAuthorityRepository sysUserAuthorityRepository) {
        super(commonRepository);
        this.sysUserAuthorityRepository = sysUserAuthorityRepository;
    }

    @Override
    public Result<List<SysUserAuthorityVo>> findByUserId(String userId) {
        return Result.of(CopyUtil.copyList(sysUserAuthorityRepository.findByUserId(userId),SysUserAuthorityVo.class));
    }

    @Override
    public Result<Boolean> saveAllByUserId(String userId, String authorityIdList) {
        //先删除旧的
        SysUserAuthorityVo sysUserAuthorityVo = new SysUserAuthorityVo();
        sysUserAuthorityVo.setUserId(userId);
        list(sysUserAuthorityVo).getData().forEach((userAuthorityVo)->{
            delete(userAuthorityVo.getUserAuthorityId());
        });

        //再保存新的
        for (String authorityId : authorityIdList.split(",")) {
            sysUserAuthorityVo.setAuthorityId(authorityId);
            save(sysUserAuthorityVo);
        }
        return Result.of(true);
    }
}
