package com.devhc.jobdeploy.utils;

import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Option;

public class ParserUtils {

    public static final Set<Class> HAS_ARGUMENT_CLASS = Sets
        .newHashSet(Integer.class, String.class, Long.class);

    public static <T> NTuple<T> getArgment(Class<?> firstClass, String[] args) {
        Map<String, Boolean> argsDefs = new HashMap<String, Boolean>();
        for (Field df : firstClass.getDeclaredFields()) {
            Option argOpt = df.getAnnotation(Option.class);
            Boolean hasArgs = HAS_ARGUMENT_CLASS.contains(df.getType());
            if (argOpt == null) {
                continue;
            }
            if (StringUtils.isNotEmpty(argOpt.name())) {
                argsDefs.put(argOpt.name(), hasArgs);
            }
            if (argOpt.aliases().length > 0) {
                for (String alias : argOpt.aliases()) {
                    argsDefs.put(alias, hasArgs);
                }
            }
        }
        List<String> parseArgs = new ArrayList<>();
        List<String> leftArgs = new ArrayList<>();
        List<String> defArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            Boolean argHasVal = argsDefs.get(args[i]);
            if ("-D".equals(args[i])) {
                defArgs.add(args[++i]);
            } else if (argHasVal == null || !args[i].startsWith("-")) {
                leftArgs.add(args[i]);
            } else {
                if (argHasVal) {
                    parseArgs.add(args[i]);
                    i++;
                    parseArgs.add(args[i]);
                } else {
                    parseArgs.add(args[i]);
                }
            }
        }
        return NTuple.make(parseArgs, leftArgs, defArgs);
    }

}
