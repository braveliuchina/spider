package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderFnCOE
 * @Description TODO
 * @Author 8203   liu_jt
 * @Date 2020/6/29 10:34
 * @Version 1.0
 */
@Data
public class SpiderFnCOE {
    private  Integer id;
    private  String fncoeName;  //名称
    private  String fncoeAge;   //年龄
    private  String fncoeNationality;  //国籍
    private  String fncoeBeyear;  //当选年
    private  String fncoeMajor;  //专业
    private  String fncoeInformation;  //详细信息
    public SpiderFnCOE(){};
    public SpiderFnCOE(Integer id, String fncoeName, String fncoeAge, String fncoeNationality, String fncoeBeyear, String fncoeMajor, String fncoeInformation) {
        this.id = id;
        this.fncoeName = fncoeName;
        this.fncoeAge = fncoeAge;
        this.fncoeNationality = fncoeNationality;
        this.fncoeBeyear = fncoeBeyear;
        this.fncoeMajor = fncoeMajor;
        this.fncoeInformation = fncoeInformation;
    }
}
