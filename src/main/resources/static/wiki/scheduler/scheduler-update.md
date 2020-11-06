**一、接口名称**
任务修改接口  

**二、接口详情**
1、修改任务  

**三、接口地址**
1、开发环境：http://192.168.3.11:8888/spider/job/edit  
2、测试环境：http://192.168.3.11:8888/spider/job/edit  
3、生产环境：http://pro.com:8080/spider/job/edit  

**四、HTTP请求方式**
POST

**五、接口参数**
|序号	|名称	|类型	|是否必须	|示例值	|描述|
|-------|-------|-------|-----------|-------|---|
|1      | id|long|是|10|任务id|
|2      | jobName|String|否|任务90|任务名称|
|3      | url|String|否|http://www.baidu.com|爬取url|
|4      | xpathList|list|否|["//div[@class='modContent']/table/tbody//tr//td/p/b/text()","//div[@class='modContent']/table/tbody//tr//td/p/span/text()"]|爬取规则列表|
|5      | jobDesc|String|否| 刘晓勇任务备注|爬取规则列表|


** 示例 **

    {
         "id": 18,
         "jobName": "张慧芳测试2",
         "jobDesc": "张慧芳的测试",
         "url": "https://www.aacsb.edu/accreditation/accredited-schools",
         "xpathList": [
             "//div[@class='modContent']/table/tbody//tr//td/p/b/text()",
             "//div[@class='modContent']/table/tbody//tr//td/p/span/text()",
             "//div[@class='modContent']/table/tbody//tr//td/p/text()"
         ]
     }


**六、返回结果**
String

**七、返回示例**
JSON示例  

  
    {
        "data": "job update successfully",
        "flag": true,
        "msg": "操作成功"
    }
    
    ///////   
    
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
