package io.github.newhoo.restkit.util;

import com.intellij.openapi.util.text.Strings;
import io.github.newhoo.restkit.common.KV;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_POSTFIX;
import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_PREFIX;

public class ToolkitUtil {

    /**
     * 将Map转为url param string
     */
    @NotNull
    public static String getRequestParam(Map<String, String> paramMap) {
        //拼装param参数
        List<String> params = new ArrayList<>();

        if (paramMap != null && paramMap.size() > 0) {
            paramMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == null || !entry.getValue().startsWith(HTTP_FILE_PREFIX))
                    .forEach(entry -> {
                        String k = encodeURLParam(entry.getKey());
                        String v = encodeURLParam(entry.getValue());
                        params.add(k + "=" + v);
                    });
        }

        return String.join("&", params);
    }

    @NotNull
    public static String encodeQueryParam(String queryParams) {
        String[] split = StringUtils.split(queryParams, '&');
        if (split == null) {
            return queryParams;
        }
        List<String> encodeQueryParams = new ArrayList<>();
        for (String pair : split) {
            String[] kv = StringUtils.split(pair, "=", 2);
            if (kv == null || kv.length <= 0) {
                continue;
            }
            if (kv.length > 1) {
                encodeQueryParams.add(encodeURLParam(kv[0]) + "=" + encodeURLParam(kv[1]));
            } else {
                encodeQueryParams.add(encodeURLParam(kv[0]) + "=");
            }
        }
        if (!encodeQueryParams.isEmpty()) {
            return Strings.join(encodeQueryParams, "&");
        }
        return queryParams;
    }

    @NotNull
    public static String encodeURLParam(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return s;
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

    public static String getUploadFileDescriptor(String filepath) {
        return HTTP_FILE_PREFIX + filepath + HTTP_FILE_POSTFIX;
    }

    public static String getUploadFilepath(String uploadFileDescriptor) {
        String s = uploadFileDescriptor.substring(HTTP_FILE_PREFIX.length());
        if (StringUtils.endsWith(s, HTTP_FILE_POSTFIX)) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }
}
