package com.devhc.jobdeploy.utils;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.NtpTimeProvider;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import org.junit.Test;

public class TotpTests {
    @Test
    public void test() throws CodeGenerationException, UnknownHostException {
        System.out.println(new Date());
        CodeGenerator cg = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
        String key = secretGenerator.generate();
        System.out.println(key);


        final TimeProvider timeProvider = new SystemTimeProvider();
        int allowedTimePeriodDiscrepancy = 1;
        int timePeriod = 30;

        final String[] sampleCodes = new String[allowedTimePeriodDiscrepancy + allowedTimePeriodDiscrepancy + 1];
        long currentBucket = Math.floorDiv(timeProvider.getTime(), timePeriod);
        for (int x = 0, i = -allowedTimePeriodDiscrepancy; i <= allowedTimePeriodDiscrepancy; i++, x++) {
            sampleCodes[x] = cg.generate(key, currentBucket);
        }
        System.out.println(Arrays.asList(sampleCodes));
    }

}
