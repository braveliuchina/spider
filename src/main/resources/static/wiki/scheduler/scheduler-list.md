#一、接口名称
任务列表查询接口

#二、接口详情
1、查询任务列表

#三、接口地址
1、开发环境：http://dev.com:8080/spidert/job/list  
2、测试环境：http://test.com:8080/spidert/job/list  
3、生产环境：http://pro.com:8080/spidert/job/list  

#四、HTTP请求方式
GET

#五、接口参数
序号	名称	类型	是否必须	示例值	描述


#六、返回结果
|序号	|名称	            |类型              |是否必须	|示例值	                |描述 |
|-------|-------------------|-----------------|---------|-----------------------|----|
|1	|id	            |long             | 是	        |2	                    |任务主键id |
|2	|jobName	     |String          | 是	        |任务88                    |任务名称
|3  |cronExpression  |String          | 是         |0/2 * * * * ?            |前端需将此cron表达式解释为执行计划 |
|4  |beanClass       |String          | 是         |testJob01                |后端任务执行类,前端不需要关注不需要展示 |
|5  |methodName      |String          | 是         |execute                 |任务执行方法,前端不需要关注不需要展示 |
|6  |jobStatus       |String          | 是         |1                        |1 正常策略调度 2 暂停 null 新建且为暂停状态 |
|7  |jobDataMap      |List            | 否         |[1602580971343, "刘晓勇"]  |任务调度方法传递的参数,前端不需要关注 |
|8  |ctime           |Long            | 是         |1602583926868             |任务创建时间毫秒时间戳 |
|9  |utime           |Long            | 是         |1602583926868             |任务修改时间毫秒时间戳 |
|10 |jobDesc         |String          | 否          |                        |任务描述 |

#七、返回示例
JSON示例  

  
    {
        "data": [  
            {  
                "id": 1,  
                "jobName": "任务88",
                "cronExpression": "0/2 * * * * ?",
                "beanClass": "testJob01",
                "methodName": "execute",
                "jobStatus": "1",
                "jobDataMap": "[1602580971343, \"刘晓勇\"]",
                "ctime": 1602583926868,
                "utime": 1602583926868,
                "jobDesc": null
            },
            {
                "id": 2,
                "jobName": "任务89",
                "cronExpression": "0/2 * * * * ?",
                "beanClass": "testJob02",
                "methodName": "execute",
                "jobStatus": "2",
                "jobDataMap": "[1602580971343, \"刘晓勇\"]",
                "ctime": 1602637955838,
                "utime": 1602637955838,
                "jobDesc": null
            },
            {
                "id": 3,
                "jobName": "任务90",
                "cronExpression": "0/5 * * * * ?",
                "beanClass": "testJob02",
                "methodName": "execute",
                "jobStatus": null,
                "jobDataMap": "[1602580971343, \"刘晓勇\"]",
                "ctime": 1602640418078,
                "utime": 1602640418078,
                "jobDesc": null
            }
        ],
        "flag": true,
        "msg": "操作成功"
    }

#八、结果代码
flag 为true 证明成功

#九、注意事项

#十、备注说明
