package com.clyrafy.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class WalletAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletAuthApplication.class, args);
    }
}