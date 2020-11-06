package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderBelgium
 * @Description TODO
 * @Author 8203   liu_jt
 * @Date 2020/7/1 9:31
 * @Version 1.0
 */
@Data
public class SpiderBelgium {
    private Integer  id;
    private String   blsGrade;
    private String   blsType;
    private String   blsTime;
    private String    blsName;
    private String   blsAwardee;
    public  SpiderBelgium(){}
    public SpiderBelgium(Integer id, String blsGrade, String blsType, String blsTime, String blsName, String blsAwardee) {
        this.id = id;
        this.blsGrade = blsGrade;
        this.blsType = blsType;
        this.blsTime = blsTime;
        this.blsName = blsName;
        this.blsAwardee = blsAwardee;
    }
}
