#一、接口名称
任务禁用接口

#二、接口详情
1、禁用任务接口

#三、接口地址
1、开发环境：http://dev.com:8080/spider/job/pause/{id}    
2、测试环境：http://test.com:8080/spider/job/pause/{id}    
3、生产环境：http://pro.com:8080/spider/job/pause/{id}    

#四、HTTP请求方式
GET

#五、接口参数
无参数,直接在url中传递id 即可


#六、返回结果
String     

#七、返回示例
JSON示例  

  
    {
        "data":"暂停定时任务成功",
        "flag": true,
        "msg": "操作成功"
    }

#八、结果代码
flag 为true 证明成功

#九、注意事项

#十、备注说明
