package com.clyrafy.wallet.org.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class OrgIdCodeGeneratorConf {

    @Value("${code.generator.length:5}")
    private int codeLength;

    @Value("${code.generator.max.attempts:50}")
    private int maxAttempts;

    @Value("${code.generator.characters:0123456789}")
    private String characters;
}