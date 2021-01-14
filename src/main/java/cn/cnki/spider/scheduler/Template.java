package cn.cnki.spider.scheduler;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "test_news_协议表")
public class Template implements Serializable {

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