**一、接口名称**
任务新增接口V2  

**二、接口详情**
1、新增任务V2  

**三、接口地址**
1、开发环境：http://192.168.3.11:8888/spider/job/add/v2  
2、测试环境：http://192.168.3.11:8888/spider/job/add/v2  
3、生产环境：http://pro.com:8080/spider/job/add/v2  

**四、HTTP请求方式**
POST

**五、接口参数**
|序号	|名称	|类型	|是否必须	|示例值	|描述|
|-------|-------|-------|-----------|-------|---|
|1      | jobName|String|是|任务90|任务名称|
|2      | cronExpression|String|否|0/5 * * * * ?|任务表达式|
|3      | beanClass|String|否|crawlService| 任务执行类名,目前先按此参数传递去验证,后续文章爬取应该是固定的值|
|4      | methodName|String|否|commonCrawl|任务执行方法名,目前先按此参数传递去验证,后续爬取应该是确定的值|
|5      | jobDataMap|String|否| {'url': 'http://www.baidu.com', 'xpathList': ['//div/a/@href', '//div/span/a/@href']}|方法参数列表 list字符串|
|6      | url|String|是|http://www.baidu.com|爬取url|
|7      | xpathList|list|否|["//div[@class='modContent']/table/tbody//tr//td/p/b/text()","//div[@class='modContent']/table/tbody//tr//td/p/span/text()"]|选择按指定规则爬取,则传此爬取规则列表字段,如不传,后台默认为爬取网页源码|
|8      | jobDesc|String|否| 刘晓勇任务备注|任务备注字段|


** 示例 **

    // 按指定规则爬取内容
    {
        "jobName": "braveliu1",
        "url": "https://www.aacsb.edu/accreditation/accredited-schools",
        "xpathList": [
            "//div[@class='modContent']/table/tbody//tr//td/p/b/text()",
            "//div[@class='modContent']/table/tbody//tr//td/p/span/text()",
            "//div[@class='modContent']/table/tbody//tr//td/p/text()"
        ],
        "jobDesc": "按xpath规则爬取网页内容"
    }   
    
    // 爬取网页源码  
    
    {
            "jobName": "braveliu1",
            "url": "https://www.aacsb.edu/accreditation/accredited-schools",
            "jobDesc": "爬取网页源码"
    }


**六、返回结果**
String

**七、返回示例**
JSON示例  

  
    {
        "data": "新增定时任务成功",
        "flag": true,
        "msg": "操作成功"
    }
    
    //////
    
    {
        "data": "",
        "flag": false,
        "msg": "duplicated job name, enter new one please"
    }


**八、结果代码**
flag 为true 证明成功  

**九、注意事项**
任务名不能重复,如有重复,则返回异常      

**十、备注说明**
