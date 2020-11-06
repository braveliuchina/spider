package cn.cnki.spider.entity;

import lombok.Data;

import java.util.List;

@Data
public class ResponseContent<T> {

	private int code;

	private List<T> data;

	private String msg;

}


