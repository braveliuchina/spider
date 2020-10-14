package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageCondition;
import lombok.Data;

import javax.persistence.Transient;
import java.io.Serializable;

@Data
public class ScheduleJobVo extends PageCondition implements Serializable {

    private Integer id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * 任务执行类（包名+类名）
     */
    private String beanClass;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 任务状态（0-停止，1-运行）
     */
    private String status;

    /**
     * 任务分组
     */
    private String jobGroup;

    /**
     * 参数
     */
    private String jobDataMap;

    /**
     * 下一次执行时间
     */

    @Transient
    private long nextfire;

}