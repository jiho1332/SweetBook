package com.sweetbook;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.sweetbook.mapper")
public class SweetBookProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SweetBookProjectApplication.class, args);
    }
}