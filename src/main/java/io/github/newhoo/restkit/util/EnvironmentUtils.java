package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.BKV;
import io.github.newhoo.restkit.common.EnvList;
import io.github.newhoo.restkit.common.EnvModel;
import io.github.newhoo.restkit.config.KeyValueTableModel;
import io.github.newhoo.restkit.config.Environment;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.newhoo.restkit.config.SettingListener.ENV_UPDATE;

/**
 * EnvironmentUtils
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class EnvironmentUtils {

    public static String formatHeaderModel(KeyValueTableModel model) {
        StringBuilder sb = new StringBuilder();
        for (Object[] objects : model.list) {
            sb.append(objects[0]).append(objects[1]).append(objects[2]);
        }
        return sb.toString();
    }

    public static String formatHeaderList(List<BKV> headerList) {
        StringBuilder sb = new StringBuilder();
        for (BKV item : headerList) {
            sb.append(item.getEnabled()).append(item.getKey()).append(item.getValue());
        }
        return sb.toString();
    }

    public static String formatEnvModelList(List<EnvModel> envModelList) {
        StringBuilder sb = new StringBuilder();
        for (EnvModel envModel : envModelList) {
            sb.append(envModel.getEnv());
            for (Object[] objects : envModel.getModel().list) {
                sb.append(objects[0]).append(objects[1]).append(objects[2]);
            }
        }
        return sb.toString();
    }

    public static String formatEnvList(List<EnvList> envList) {
        StringBuilder sb = new StringBuilder();
        for (EnvList list : envList) {
            sb.append(list.getEnv());
            for (BKV item : list.getItems()) {
                sb.append(item.getEnabled()).append(item.getKey()).append(item.getValue());
            }
        }
        return sb.toString();
    }

    public static List<EnvList> buildEnvList(List<EnvModel> envModelList) {
        List<EnvList> list = new ArrayList<>();
        for (EnvModel envModel : envModelList) {
            List<BKV> items = new ArrayList<>();
            for (Object[] objects : envModel.getModel().list) {
                items.add(new BKV((Boolean) objects[0], (String) objects[1], (String) objects[2]));
            }
            list.add(new EnvList(envModel.getEnv(), items));
        }
        return list;
    }

    public static List<BKV> buildHeaderList(KeyValueTableModel model) {
        List<BKV> headers = new ArrayList<>();
        for (Object[] objects : model.list) {
            headers.add(new BKV((Boolean) objects[0], (String) objects[1], (String) objects[2]));
        }
        return headers;
    }

    public static List<BKV> parseHeaderList(String formatString) {
        List<BKV> headers = new ArrayList<>();

        String[] split = StringUtils.split(formatString, "@@@");
        if (split == null || split.length % 3 != 0) {
            return headers;
        }

        for (int i = 0; i < split.length; i = i + 3) {
            if (StringUtils.isNotBlank(split[i])) {
                headers.add(new BKV(BooleanUtils.toBoolean(split[i]), StringUtils.trimToEmpty(split[i + 1]), StringUtils.trimToEmpty(split[i + 2])));
            }
        }
        return headers;
    }

    public static List<EnvList> parseEnvList(String formatString) {
        List<EnvList> list = new ArrayList<>();

        String[] split = StringUtils.split(formatString, "@@@");
        if (split == null || split.length % 4 != 0) {
            return list;
        }

        Map<String, List<BKV>> envMap = new HashMap<>();
        for (int i = 0; i < split.length; i = i + 4) {
            List<BKV> items = envMap.getOrDefault(split[i], new ArrayList<>());
            if (StringUtils.isNotBlank(split[i + 1])) {
                items.add(new BKV(BooleanUtils.toBoolean(split[i + 1]), StringUtils.trimToEmpty(split[i + 2]), StringUtils.trimToEmpty(split[i + 3])));
            }
            envMap.putIfAbsent(split[i], items);
        }
        envMap.forEach((k, v) -> list.add(new EnvList(k, v)));
        return list;
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(".*\\{\\{(.+)}}+.*");

    /**
     * 处理占位变量
     *
     * @param original
     * @param project
     */
    public static String handlePlaceholderVariable(String original, Project project) {
        if (StringUtils.isEmpty(original)
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

        Environment setting = Environment.getInstance(project);
        Map<String, String> env = setting.getCurrentEnabledEnvMap();
        Map<String, Method> scriptMethodMap = (StringUtils.startsWith(original, "{{$") && StringUtils.endsWith(original, "$}}")) ? setting.getScriptMethodMap() : Collections.emptyMap();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(original);
        while (matcher.find()) {
            String group = matcher.group(1);
            String tmpGroup = "{{" + group + "}}";
            searchList.add(tmpGroup);
            replacementList.add(handleScriptVariable(env.getOrDefault(group, tmpGroup), scriptMethodMap));
        }
        return StringUtils.replaceEach(original, searchList.toArray(new String[0]), replacementList.toArray(new String[0]));
    }

    /**
     * 占位变量 - 预设函数
     *
     * @param presetVariable
     */
    private static String handlePresetVariable(String presetVariable) {
        return StringUtils.replaceEach(presetVariable,
                new String[]{"{{$uuid}}", "{{$timestamp}}"},
                new String[]{UUID.randomUUID().toString(), String.valueOf(System.currentTimeMillis())}
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

    public static void notifyEnvUpdate(Project project) {
        if (project.isDefault()) {
            return;
        }
        project.getMessageBus().syncPublisher(ENV_UPDATE).changeEnv();
    }
}