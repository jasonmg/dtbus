package com.hawker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@EnableTransactionManagement // 启注解事务管理
@SpringBootApplication
@ComponentScan(basePackages = "com.hawker")
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
		logger.info("SpringBoot server stated on port: 8080");
	}

	// 增加一个SpringMVC的DispatcherServlet，接收前台/api开头的请求
	@Bean
	public ServletRegistrationBean apiV1ServletBean(WebApplicationContext wac) {
		DispatcherServlet servlet = new DispatcherServlet(wac);

		ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/api/*");
		bean.setName("ApiServlet");
		return bean;
	}

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return container -> {
            container.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/error/404"));
            container.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500"));
            container.addErrorPages(new ErrorPage(Exception.class, "/error/500"));
        };
    }

}
