package cn.cnki.spider.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("testJob01")
@Transactional
public class TestJob01 {

    public void execute(Long time, String name) {
        System.out.println("-------------------TestJob01任务执行开始-------------------");
        System.out.println(name + ":" + time);
        System.out.println("-------------------TestJob01任务执行结束-------------------");
    }
}