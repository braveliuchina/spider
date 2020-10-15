#一、接口名称
任务新增接口  

#二、接口详情
1、新增任务  

#三、接口地址
1、开发环境：http://dev.com:8080/spidert/job/add  
2、测试环境：http://test.com:8080/spidert/job/add  
3、生产环境：http://pro.com:8080/spidert/job/add  

#四、HTTP请求方式
POST

#五、接口参数
|序号	|名称	|类型	|是否必须	|示例值	|描述|
|-------|-------|-------|-----------|-------|---|
|1      | jobName|String|是|任务90|任务名称|
|2      | cronExpression|String|是|0/5 * * * * ?|任务表达式|
|1      | beanClass|String|是|testJob02| 任务执行类名,目前先按此参数传递去验证,后续文章爬取应该是固定的值|
|1      | methodName|String|是|execute|任务执行方法名,目前先按此参数传递去验证,后续爬取应该是确定的值|
|1      | jobDataMap|String|是|[1602580971343, "刘晓勇"]|方法参数列表 list字符串|

#六、返回结果
String

#七、返回示例
JSON示例  

  
    {
        "data": "新增任务成功",
        "flag": true,
        "msg": "操作成功"
    }

#八、结果代码
flag 为true 证明成功

#九、注意事项

#十、备注说明
