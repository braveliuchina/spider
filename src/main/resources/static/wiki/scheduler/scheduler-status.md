#一、接口名称
任务状态查询接口

#二、接口详情
1、查询任务状态

#三、接口地址
1、开发环境：http://dev.com:8080/spider/job/status/{id}    
2、测试环境：http://test.com:8080/spider/job/status/{id}   
3、生产环境：http://pro.com:8080/spider/job/status/{id}   

#四、HTTP请求方式
GET

#五、接口参数
无 直接在url中传递id即可


#六、返回结果

int  
0 未启用或未执行(如定时任务)  
1 正在执行   
2 执行完成(临时任务)  

#七、返回示例
JSON示例  

  
    {
        "data": 1,
        "flag": true,
        "msg": "操作成功"
    }

#八、结果代码
flag 为true 证明成功

#九、注意事项

#十、备注说明
