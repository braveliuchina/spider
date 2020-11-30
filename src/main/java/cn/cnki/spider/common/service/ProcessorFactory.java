package cn.cnki.spider.common.service;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.spider.AbstractCloudNewspaperProcessor;
import cn.cnki.spider.spider.AbstractNewspaperProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
@RequiredArgsConstructor
public class ProcessorFactory {

    private final SpiderConfigDao spiderConfigDao;

    public AbstractCloudNewspaperProcessor buildProcessor(String newspaperName) {
        AbstractCloudNewspaperProcessor processor = new AbstractCloudNewspaperProcessor(spiderConfigDao);
        processor.setNewspaperName(newspaperName);
        return processor;
    }
}