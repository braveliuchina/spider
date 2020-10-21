package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderCAS
 * @Description TODO  中国科学院cas
 * @Author 8203   liu_jt
 * @Date 2020/6/28 13:45
 * @Version 1.0
 */
@Data
public class SpiderCAS {
    private  Integer  id;
    private  String  casSubject; //学科
    private  String  casName; //院士名称
    private  String  casInformation;  //详细信息
    public SpiderCAS(){}
    public SpiderCAS(Integer id, String casSubject, String casName, String casInformation) {
        this.id = id;
        this.casSubject = casSubject;
        this.casName = casName;
        this.casInformation = casInformation;
    }
}
