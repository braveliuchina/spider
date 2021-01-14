package cn.cnki.spider.scheduler;

import cn.cnki.spider.common.pojo.PageCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateVO  extends PageCondition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 任务名称
	 */
	private String code;

	/**
	 * 定时任务: sceduler
	 * 普通任务 temp
	 */
	private String name;

	private String icon;
	/**
	 * 创建时间
	 */
	private Long ctime;

	/**
	 * 更新时间
	 */
	private Long utime;
	
	private String display;


}