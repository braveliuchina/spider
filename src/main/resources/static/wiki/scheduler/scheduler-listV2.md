**一、接口名称**
任务列表查询V2接口

**二、接口详情**
1、任务列表查询V2接口

**三、接口地址**
1、开发环境：http://dev.com:8080/spider/job/list/v2  
2、测试环境：http://test.com:8080/spider/job/list/v2  
3、生产环境：http://pro.com:8080/spider/job/list/v2  

**四、HTTP请求方式**
GET

**五、接口参数**
序号	名称	类型	是否必须	示例值	描述  
|-------|-------------------|-----------------|---------|-----------------------|----|  
|1	|page	            |int             | 是	        |1	                    |页码从1开始 |  
|2	|rows	     |int          | 是	        |2                    |记录数 |  

**六、参数示例**


    {
        "page": 2,
        "rows": 2
    }

**七、返回结果**
|序号	|名称	            |类型              |是否必须	|示例值	                |描述 |
|-------|-------------------|-----------------|---------|-----------------------|----|
|1	|id	            |long             | 是	        |2	                    |任务主键id |
|2	|jobName	     |String          | 是	        |任务88                    |任务名称
|3  |cronExpression  |String          | 是         |0/2 * * * * ?            |前端需将此cron表达式解释为执行计划 |  
|4  |beanClass       |String          | 是         |testJob01                |后端任务执行类,前端不需要关注不需要展示 |
|5  |methodName      |String          | 是         |execute                 |任务执行方法,前端不需要关注不需要展示 |
|6  |jobStatus       |String          | 是         |1                        |0 未执行 1 正在执行 2 执行完成 |
|7  |jobDataMap      |List            | 否         |[1602580971343, "刘晓勇"]  |任务调度方法传递的参数,前端不需要关注 |
|8  |ctime           |Long            | 是         |1602583926868             |任务创建时间毫秒时间戳 |
|9  |utime           |Long            | 是         |1602583926868             |任务修改时间毫秒时间戳 |
|10 |jobDesc         |String          | 否          |                        |任务描述 |
|11 |category        |String          | 是          | 按模板                       |直接此字段展示即可 |
|12 |err             |String          | 是          |  暂无异常信息                      |直接展示此字段即可 |
|13 |jobType         |String          | 是          |  temp或scheduler                      |temp启动执行 scheduler定时执行|


**八、返回示例**
JSON示例  

  
    {
        "data": {
            "page": 2,
            "pageSize": 2,
            "sidx": null,
            "sord": null,
            "rows": [
                {
                    "page": 1,
                    "rows": 10,
                    "sidx": null,
                    "sord": null,
                    "id": 14,
                    "jobName": "braveliu2",
                    "jobType": "temp",
                    "cronExpression": "0/2 * * * * ? 2030",
                    "beanClass": "crawlService",
                    "methodName": "commonCrawlV2",
                    "jobStatus": "2",
                    "jobDataMap": "[\"https://www.aacsb.edu/accreditation/accredited-schools\",[\"//div[@class='modContent']/table/tbody//tr//td/p/b/text()\",\"//div[@class='modContent']/table/tbody//tr//td/p/span/text()\",\"//div[@class='modContent']/table/tbody//tr//td/p/text()\"]]",
                    "ctime": 1603875507719,
                    "utime": 1603875507719,
                    "jobDesc": null,
                    "url": null,
                    "xpathList": null,
                    "pageable": {
                        "sort": {
                            "sorted": false,
                            "unsorted": true,
                            "empty": true
                        },
                        "offset": 0,
                        "pageNumber": 0,
                        "pageSize": 10,
                        "unpaged": false,
                        "paged": true
                    }
                },
                {
                    "page": 1,
                    "rows": 10,
                    "sidx": null,
                    "sord": null,
                    "id": 18,
                    "jobName": "测试1",
                    "jobType": "temp",
                    "cronExpression": "0/2 * * * * ? 2030",
                    "beanClass": "crawlService",
                    "methodName": "commonCrawlV2",
                    "jobStatus": null,
                    "jobDataMap": "[\"https://www.baidu.com/\",[\"//div[@class='modContent']/table/tbody//tr//td/p/b/text()\",\"//div[@class='modContent']/table/tbody//tr//td/p/span/text()\"]]",
                    "ctime": 1603951997167,
                    "utime": 1603951997167,
                    "jobDesc": null,
                    "url": null,
                    "xpathList": null,
                    "pageable": {
                        "sort": {
                            "sorted": false,
                            "unsorted": true,
                            "empty": true
                        },
                        "offset": 0,
                        "pageNumber": 0,
                        "pageSize": 10,
                        "unpaged": false,
                        "paged": true
                    }
                }
            ],
            "records": 4,
            "total": 2
        },
        "flag": true,
        "msg": "操作成功"
    }
    
records 为总记录条数    

**九、结果代码**
flag 为true 证明成功

**十、注意事项**

**十、备注说明**
