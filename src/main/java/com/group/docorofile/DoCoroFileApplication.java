package com.group.docorofile;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:.env", ignoreResourceNotFound = true)
public class DoCoroFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(DoCoroFileApplication.class, args);
    }

}
