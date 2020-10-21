package cn.cnki.spider.common.service;

import cn.cnki.spider.dao.SpiderConfigDao;
import cn.cnki.spider.spider.AbstractNewspaperProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessorFactory {

    private final SpiderConfigDao spiderConfigDao;

    public AbstractNewspaperProcessor buildProcessor(String newspaperName) {
        AbstractNewspaperProcessor processor = new AbstractNewspaperProcessor(spiderConfigDao);
        processor.setNewspaperName(newspaperName);
        return processor;
    }
}