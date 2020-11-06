package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderCOE
 * @Description TODO   中国工程院
 * @Author 8203   liu_jt
 * @Date 2020/6/29 9:43
 * @Version 1.0
 */
@Data
public class SpiderCOE {
   private  Integer  id;
    private  String  coeSubject; //学科
    private  String  coeName; //院士名称
    private  String  coeInformation;  //详细信息
    public SpiderCOE(){};
    public SpiderCOE(Integer id, String coeSubject, String coeName, String coeInformation) {
        this.id = id;
        this.coeSubject = coeSubject;
        this.coeName = coeName;
        this.coeInformation = coeInformation;
    }
}
