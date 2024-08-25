package io.github.newhoo.restkit.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EnvironmentUtils
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class EnvironmentUtils {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(".*\\{\\{(.+)}}+.*");

    /**
     * 处理占位变量
     *
     * @param original
     * @param currentEnvMap
     * @param scriptMethodMap
     */
    public static String handlePlaceholderVariable(String original, Map<String, String> currentEnvMap, Map<String, Method> scriptMethodMap) {
        if (StringUtils.isEmpty(original)
                || (currentEnvMap == null && scriptMethodMap == null)
                || !StringUtils.contains(original, "{{") || !StringUtils.contains(original, "}}")) {
            return original;
        }

        // 预设函数
        original = handlePresetVariable(original);

        if (!StringUtils.contains(original, "{{") || !StringUtils.contains(original, "}}")) {
            return original;
        }

        List<String> searchList = new ArrayList<>(2);
        List<String> replacementList = new ArrayList<>(2);

//        Map<String, String> currentEnvMap = environment.getCurrentEnabledEnvMap(currentEnv);
//        Map<String, Method> scriptMethodMap = (StringUtils.contains(original, "{{$") && StringUtils.contains(original, "$}}")) ? environment.getScriptMethodMap() : Collections.emptyMap();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(original);
        while (matcher.find()) {
            String group = matcher.group(1);
            String tmpGroup = "{{" + group + "}}";
            searchList.add(tmpGroup);
            replacementList.add(handleScriptVariable(currentEnvMap.getOrDefault(group, tmpGroup), scriptMethodMap));
        }
        return StringUtils.replaceEach(original, searchList.toArray(new String[0]), replacementList.toArray(new String[0]));
    }

    /**
     * 占位变量 - 预设函数
     *
     * @param presetVariable
     */
    private static String handlePresetVariable(String presetVariable) {
        String uuid = UUID.randomUUID().toString();
        long currentTimeMillis = System.currentTimeMillis();
        return StringUtils.replaceEach(presetVariable,
                new String[]{"{{$uuid}}", "{{$uuid-}}", "{{$timestamp}}", "{{$timestamp/3}}"},
                new String[]{uuid, uuid.replace("-", ""), String.valueOf(currentTimeMillis), String.valueOf(currentTimeMillis / 1000)}
        );
    }

    /**
     * 占位变量 - 处理脚本变量
     *
     * @param scriptVariable
     * @param scriptMethodMap
     */
    private static String handleScriptVariable(String scriptVariable, Map<String, Method> scriptMethodMap) {
        // 预设函数
        scriptVariable = handlePresetVariable(scriptVariable);
        if (!StringUtils.startsWith(scriptVariable, "{{$") || !StringUtils.endsWith(scriptVariable, "$}}")) {
            return scriptVariable;
        }
        String scriptMethod = scriptVariable.substring(3, scriptVariable.lastIndexOf("$"));
        if (!scriptMethodMap.containsKey(scriptMethod)) {
            return scriptVariable;
        }

        try {
            Object result3 = scriptMethodMap.get(scriptMethod).invoke(null);
            return String.valueOf(result3);
        } catch (Exception e) {
            e.printStackTrace();
            return scriptVariable;
        }
    }
}