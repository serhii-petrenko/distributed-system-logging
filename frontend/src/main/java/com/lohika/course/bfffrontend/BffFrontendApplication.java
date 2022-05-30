package com.lohika.course.bfffrontend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class BffFrontendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffFrontendApplication.class, args);
    }

    @Bean
    public WebClient authorClient(@Value("${authors.url}") String authorsUrl) {
        return WebClient
                .builder()
                .baseUrl(authorsUrl)
                .build();
    }

    @Bean
    public WebClient bookClient(@Value("${books.url}") String booksUrl) {
        return WebClient
                .builder()
                .baseUrl(booksUrl)
                .build();
    }
}
