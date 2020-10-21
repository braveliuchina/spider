package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderFnCAS
 * @Description TODO
 * @Author 8203   liu_jt
 * @Date 2020/6/28 17:18
 * @Version 1.0
 */
@Data
public class SpiderFnCAS {
    private  Integer id;
    private  String  fncasName;
    private  String  fncasProfile;
    public SpiderFnCAS(){};
    public SpiderFnCAS(Integer id, String fncasName, String fncasProfile) {
        this.id = id;
        this.fncasName = fncasName;
        this.fncasProfile = fncasProfile;
    }
}
