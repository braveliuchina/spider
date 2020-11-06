package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderCYC
 * @Description TODO
 * @Author 8203   liu_jt
 * @Date 2020/7/1 14:48
 * @Version 1.0
 */
@Data
public class SpiderCYC {
    private Integer  id;
    private String  time;
    private  String  type;
    private String  name;
    private String zhiwei;
    private String  chengguo ;
    private String  xinxi ;
    public SpiderCYC(){}

    public SpiderCYC(Integer id, String time, String type, String name, String zhiwei, String chengguo, String xinxi) {
        this.id = id;
        this.time = time;
        this.type = type;
        this.name = name;
        this.zhiwei = zhiwei;
        this.chengguo = chengguo;
        this.xinxi = xinxi;
    }
}
