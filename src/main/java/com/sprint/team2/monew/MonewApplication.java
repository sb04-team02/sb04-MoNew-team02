package com.sprint.team2.monew;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonewApplication {

    @PostConstruct
    public void started() {
        // JVM의 기본 시간대를 한국으로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }


    public static void main(String[] args) {
        SpringApplication.run(MonewApplication.class, args);
    }

}
