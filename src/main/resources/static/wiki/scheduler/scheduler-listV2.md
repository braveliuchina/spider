**一、接口名称**
任务列表查询V2接口

**二、接口详情**
1、任务列表查询V2接口

**三、接口地址**
1、开发环境：http://dev.com:8080/spider/job/list/v2?page=1&rows=12&type=3    
2、测试环境：http://test.com:8080/spider/job/list/v2?page=1&rows=12&type=3    
3、生产环境：http://pro.com:8080/spider/job/list/v2?page=1&rows=12&type=3    

**四、HTTP请求方式**
GET

**五、接口参数**
序号	名称	类型	是否必须	示例值	描述  
|-------|-------------------|-----------------|---------|-----------------------|----|  
|1	|page	            |int             | 是	        |1	                    |页码从1开始 |  
|2	|rows	     |int          | 是	        |2                    |记录数 |  
|3	|type	     |int          | 否	        |2                    |任务类型  type不传 我的任务, 任务筛选 云行中1 已停止2 已完成 3|  


**六、参数示例**


    spider/job/list/v2?page=1&rows=12&type=3

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
|14 |result          |int          | 是          |  1                      |是否有执行结果 如>1 查看结果和查看历史记录按钮都有|  
|15 |strategyDesc    |String          | 是          |  执行一次                      |执行策略字段|
|16 |enable          |int          | 否          |  1或null 或0                      |0或null未启用 1 启用 (针对定时任务,临时任务此字段不起作用,参照jobStatus字段)|
|17 |loginName          |用户名          | 是          |  sa                      |用户名|
|18 |templateName          |模板名称          | 否          |  山西日报                      |按模板 爬取类型一定返回的字段, 模板名称|  
|19 |his             |int          |是            | 1 | 历史执行次数,注意: 可能为空,为空时 视为0|


**八、返回示例**
JSON示例  

  
    {
        "data": {
            "page": 2,
            "pageSize": 2,
            "sidx": null,
            "sord": null,
            "rows": [{
                     "page": 1,
                     "rows": 10,
                     "sidx": null,
                     "sord": null,
                     "id": 42,
                     "jobName": "太原日报模板抓取测试",
                     "jobType": "temp",
                     "category": "按模板",
                     "loginName": "sa",
                     "cronExpression": "0/2 * * * * ? 2030",
                     "strategyDesc": "执行一次",
                     "beanClass": "crawlService",
                     "methodName": "templateCrawl",
                     "jobStatus": "2",
                     "jobDataMap": "[1]",
                     "ctime": 1604979780754,
                     "utime": 1605343633062,
                     "jobDesc": "太原日报当前日期整份报纸临时抓取",
                     "enable": 1,
                     "result": 1,
                     "his": 1,
                     "err": "",
                     "url": null,
                     "templateId": 1,
                     "templateName": "太原日报",
                     "templateByDate": false,
                     "skipOnErr": false,
                     "xpathList": null,
                     "pageable": {
                         "sort": {
                             "unsorted": true,
                             "sorted": false,
                             "empty": true
                         },
                         "offset": 0,
                         "pageSize": 10,
                         "pageNumber": 0,
                         "paged": true,
                         "unpaged": false
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
