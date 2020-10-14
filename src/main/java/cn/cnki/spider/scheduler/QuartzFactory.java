package cn.cnki.spider.scheduler;

import cn.cnki.spider.util.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Component
public class QuartzFactory implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        //获取调度数据
        ScheduleJob scheduleJob = (ScheduleJob) jobExecutionContext.getMergedJobDataMap().get("scheduleJob");

        String jobStatus = scheduleJob.getJobStatus();
        if (StringUtils.isEmpty(jobStatus) || !"1".equals(jobStatus)) {
            return;
        }
        //获取对应的Bean
        Object object = SpringUtil.getBean(scheduleJob.getBeanClass());
        try {

            Method[] methods = object.getClass().getDeclaredMethods();
            String methodName = scheduleJob.getMethodName();
            for (int i=0;i< methods.length;i++) {
                String methodNameIter =methods[i].getName();
                if (!methodName.equals(methodNameIter)) {
                    continue;
                }
                Parameter[] parameters = methods[i].getParameters();

                if (parameters.length == 0) {
                    methods[i].invoke(object);
                    return;
                }
                Object[] actualParameters = new Object[parameters.length] ;
                String jobDataJsonString = scheduleJob.getJobDataMap();
                JSONArray json = JSONArray.parseArray(jobDataJsonString);
                for (int j = 0; j < parameters.length; j++) {
                    actualParameters[j] = json.get(j);
                }
                methods[i].invoke(object, actualParameters);
            }

        } catch (Exception e) {
            log.warn("cron job error", e);
        }
    }
}