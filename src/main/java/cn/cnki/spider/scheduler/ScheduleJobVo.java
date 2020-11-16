package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class ScheduleJobVo extends PageCondition {

    private Long id;

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 定时任务: sceduler
     * 普通任务 temp
     */
    private String jobType;

    private String loginName;

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
    private String jobStatus;


    /**
     * 参数
     */
    private String jobDataMap;

    /**
     * 创建时间
     */
    private Long ctime;

    /**
     * 更新时间
     */
    private Long utime;

    /**
     * 描述
     */
    private String jobDesc;

    // 定时任务是否启用
    private Integer enable;
    // 定时任务执行结果数量
    private Integer result;

    // 任务执行次数
    private Integer his;
    // 最近一次执行错误信息
    private String err;

    private String url;

    private List<String> xpathList;

}