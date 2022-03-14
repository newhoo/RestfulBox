package io.github.newhoo.restkit.util;

import io.github.newhoo.restkit.common.KV;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolkitUtil {

    /**
     * 将Map转为url param string
     */
    @NotNull
    public static String getRequestParam(Map<String, String> paramMap) {
        //拼装param参数
        List<String> params = new ArrayList<>();

        if (paramMap != null && paramMap.size() > 0) {
            paramMap.forEach((k, v) -> params.add(k + "=" + v));
        }

        return String.join("&", params);
    }

    /**
     * 将文本转为Map，在脚本中可更新
     */
    @NotNull
    public static Map<String, String> textToModifiableMap(String text) {
        Map<String, String> map = new HashMap<>(8);
        if (StringUtils.isBlank(text)) {
            return map;
        }
        String[] lines = text.split("\n");

        for (String line : lines) {
            String[] prop = line.split(":", 2);
            if (prop.length == 0) {
                continue;
            }
            String key = prop[0].trim();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            map.put(key, "");

            if (prop.length > 1) {
                map.put(key, prop[1].trim());
            }
        }
        return map;
    }

    /**
     * 将文本转为KV list
     */
    @NotNull
    public static List<KV> textToKVList(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        List<KV> list = new ArrayList<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            String[] prop = line.split(":", 2);
            if (prop.length == 0) {
                continue;
            }
            String key = prop[0].trim();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            KV kv = new KV((key), "");

            if (prop.length > 1) {
                kv.setValue(prop[1].trim());
            }
            list.add(kv);
        }
        return list;
    }
}
