package cn.cnki.spider.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("testJob02")
@Transactional
public class TestJob02 {

    public void execute() {
        System.out.println("-------------------TestJob02任务执行开始-------------------");
        System.out.println(System.currentTimeMillis());
        System.out.println("-------------------TestJob02任务执行结束-------------------");
    }
}