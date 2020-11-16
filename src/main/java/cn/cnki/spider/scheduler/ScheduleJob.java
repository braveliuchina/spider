package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "crawl_job")
public class ScheduleJob implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	private String jobDesc;
	// 定时任务是否启用
	private Integer enable;
	// 任务执行成功数量
	private Integer result;
	// 任务执行次数
	private Integer his;
	// 最近一次执行错误信息
	private String err;

	/**
	 * 创建时间
	 */
	private Long ctime;

	/**
	 * 更新时间
	 */
	private Long utime;


}