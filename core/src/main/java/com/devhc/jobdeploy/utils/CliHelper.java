package com.devhc.jobdeploy.utils;

import com.devhc.jobdeploy.config.DeployCustomConfig;

import java.io.Console;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CliHelper {

    /**
     * Parse CLI::ask() CLI::ask("question")
     */
    public static String parseAsk(String line, String defalut) {
        Pattern pattern = Pattern.compile("CLI::ask\\((['\"])?(.*)\\1\\)");
        Matcher matcher = pattern.matcher(line);
        String msg = "";
        String value = null;
        if ((matcher.matches() && matcher.groupCount() > 0)) {
            msg = matcher.group(2);
            if ("".equals(msg)) {
                msg = defalut;
            }
            System.out.println(msg);
            Scanner scanner = new Scanner(System.in);
            value = scanner.nextLine();
        } else if (line.equals("CLI::ask()")) {
            System.out.println(defalut);
            Scanner scanner = new Scanner(System.in);
            value = scanner.nextLine();
        }

        return value;
    }

    public static String convertPropToEnvName(String name) {
        return name.toUpperCase()
                .replace(".", "_")
                .replace("-", "_");
    }


    public static String askPassword() {
        Console console = System.console();
        console.printf("please input password:\n");
        String password = new String(console.readPassword());
        return password;
    }

    /**
     * Parse CLI::custom() CLI::custom("customkey")
     */
    public static String parseCustom(String line, String key, String defaultTips,
                                     DeployCustomConfig customConfig)
            throws IOException {
        Pattern pattern = Pattern.compile("CLI::custom\\((['\"])?(.*)\\1\\)");
        Matcher matcher = pattern.matcher(line);
        String msg = "";
        String value = null;
        String customKey = key;
        if ((matcher.matches() && matcher.groupCount() > 0)) {
            if (!"".equals(matcher.group(2))) {
                customKey = matcher.group(2);
            }
            String customValue = customConfig.getCustomConfig(customKey);
            if (customValue != null) {
                return customValue;
            }
            String envName = convertPropToEnvName(customKey);
            Map<String, String> envs = System.getenv();
            if (envs.containsKey(envName)) {
                return System.getenv(envName);
            }

            System.out.println("please input custom " + customKey + "?");
            Console console = System.console();
            if (customKey.contains("password")) {
                value = String.valueOf(console.readPassword());
            } else {
                value = console.readLine();
            }
//      Scanner scanner = new Scanner(System.in);
//      value = scanner.nextLine();
            customConfig.setCustomConfig(customKey, value);
        } else if (line.equals("CLI::custom()")) {
            String customValue = customConfig.getCustomConfig(customKey);
            if (customValue != null) {
                return customValue;
            }
            System.out.println(defaultTips);
//      Scanner scanner = new Scanner(System.in);
//      value = scanner.nextLine();
            Console console = System.console();
            if (customKey.contains("password")) {
                value = String.valueOf(console.readPassword());
            } else {
                value = console.readLine();
            }
            customConfig.setCustomConfig(customKey, value);
        }

        return value;
    }
}
