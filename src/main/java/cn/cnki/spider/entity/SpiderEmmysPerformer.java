package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderEmmysPerformer
 * @Description TODO  艾美奖演员实体类
 * @Author 8203   liu_jt
 * @Date 2020/6/29 13:00
 * @Version 1.0
 */
@Data
public class SpiderEmmysPerformer {
    private  Integer id;
    private  String  time;  //获奖时间
    private  String  session; //获奖届次
    private  String  type;  //获奖类型
    private  String  awardee; //获奖人
    private  String  name;  //获奖名称
    private  String  grade; //获奖等级
    public SpiderEmmysPerformer() {
    }

    public SpiderEmmysPerformer(Integer id, String time, String session, String type, String awardee, String name, String grade) {
        this.id = id;
        this.time = time;
        this.session = session;
        this.type = type;
        this.awardee = awardee;
        this.name = name;
        this.grade = grade;
    }
}
