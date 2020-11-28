package cn.cnki.spider.config;

import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration

public class ErrorPageConfig {


    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {

        //第二种写法：java8 lambda写法
        return (factory -> {

            ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/index.html");

            factory.addErrorPages(errorPage404);

        });
    }

}