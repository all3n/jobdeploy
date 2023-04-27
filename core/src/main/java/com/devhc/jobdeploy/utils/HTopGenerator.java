package com.devhc.jobdeploy.utils;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import org.slf4j.Logger;

public class HTopGenerator {

    public static Logger log = Loggers.get();

    int timePeriod = 30;
    private final DefaultCodeGenerator codeGenerator;
    private final SystemTimeProvider timeProvider;
    String secret;

    public HTopGenerator(String secret) {
        this.secret = secret;
        this.codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        this.timeProvider = new SystemTimeProvider();
    }

    public String genCode() {
        long currentBucket = Math.floorDiv(timeProvider.getTime(), timePeriod);
        try {
            return codeGenerator.generate(secret, currentBucket);
        } catch (CodeGenerationException e) {
            log.error("code gen error", e);
            return null;
        }
    }
}
