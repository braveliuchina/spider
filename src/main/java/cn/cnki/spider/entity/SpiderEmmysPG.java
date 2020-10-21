package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderEmmysPG
 * @Description TODO   艾美奖节目实体类
 * @Author 8203   liu_jt
 * @Date 2020/6/29 12:56
 * @Version 1.0
 */
@Data
public class SpiderEmmysPG {
    private  Integer id;
    private  String  time;  //获奖时间
    private  String  session; //获奖届次
    private  String  type;  //获奖类型
    private  String  name;  //获奖名称
    private  String  grade; //获奖等级
    public SpiderEmmysPG(){}
    public SpiderEmmysPG(Integer id, String time, String session, String type, String name, String grade) {
        this.id = id;
        this.time = time;
        this.session = session;
        this.type = type;
        this.name = name;
        this.grade = grade;
    }
}
