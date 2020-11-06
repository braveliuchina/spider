package cn.cnki.spider.entity;

import lombok.Data;

/**
 * @ClassName SpiderOUZ
 * @Description TODO
 * @Author 8203   liu_jt
 * @Date 2020/7/2 9:15
 * @Version 1.0
 */
@Data
public class SpiderOUZ {
    private  Integer  id;
    private  String   company;
    private  String   country;
    private String   region;
    private  String  address;
    private String  website;
    private  String  state;
    public  SpiderOUZ(){}
    public SpiderOUZ(Integer id, String company, String country, String region, String address, String website, String state) {
        this.id = id;
        this.company = company;
        this.country = country;
        this.region = region;
        this.address = address;
        this.website = website;
        this.state = state;
    }
}
