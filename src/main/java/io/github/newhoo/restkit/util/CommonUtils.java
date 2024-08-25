package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class CommonUtils {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatLocalDateTime(Long ts) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(ts).atZone(ZoneOffset.systemDefault()).toLocalDateTime();
        return localDateTime.format(DEFAULT_DATE_TIME_FORMATTER);
    }

    public static List<String> getSpringContextPath(Project project) {
        Collection<VirtualFile> propertiesList = FilenameIndex.getAllFilesByExt(project, "properties", GlobalSearchScope.projectScope(project));
        Collection<VirtualFile> yamlList1 = FilenameIndex.getAllFilesByExt(project, "yaml", GlobalSearchScope.projectScope(project));
        Collection<VirtualFile> yamlList2 = FilenameIndex.getAllFilesByExt(project, "yml", GlobalSearchScope.projectScope(project));
        return Stream.of(propertiesList, yamlList1, yamlList2)
                     .flatMap(Collection::stream)
                     //.filter(f -> f.getName().startsWith("application"))
                     .map(f -> {
                         try (InputStream is = f.getInputStream()) {
                             if (f.getName().endsWith("properties")) {
                                 Properties properties = new Properties();
                                 properties.load(is);
                                 String path = StringUtils.defaultIfEmpty(properties.getProperty("server.servlet.context-path"), properties.getProperty("server.context-path"));
                                 String port = StringUtils.defaultIfEmpty(properties.getProperty("server.servlet.port"), properties.getProperty("server.port"));
                                 if (StringUtils.isAllEmpty(path, port)) {
                                     return null;
                                 }
                                 return "http://localhost" + ":" + StringUtils.defaultIfEmpty(port, "8080") + StringUtils.defaultString(path);
                             } else {
                                 Properties properties = new Yaml().loadAs(is, Properties.class);
                                 Object path = ObjectUtils.defaultIfNull(getValueByKey("server.servlet.context-path", properties), getValueByKey("server.context-path", properties));
                                 Object port = ObjectUtils.defaultIfNull(getValueByKey("server.servlet.port", properties), getValueByKey("server.port", properties));
                                 if (ObjectUtils.anyNotNull(path, port)) {
                                     return "http://localhost" + ":" + StringUtils.defaultIfEmpty(port != null ? String.valueOf(port) : null, "8080") + StringUtils.defaultString(path != null ? String.valueOf(path) : null);
                                 }
                                 return null;
                             }
                         } catch (Exception exception) {
                             exception.printStackTrace();
                         }
                         return null;
                     })
                     .filter(StringUtils::isNotEmpty)
                     .distinct()
                     .collect(Collectors.toList());
    }

    private Object getValueByKey(String key, Properties properties) {
        String separator = ".";
        String[] separatorKeys = null;
        if (key.contains(separator)) {
            separatorKeys = key.split("\\.");
        } else {
            return properties.get(key);
        }
        Map<String, Map<String, Object>> finalValue = new HashMap<>();
        for (int i = 0; i < separatorKeys.length - 1; i++) {
            if (i == 0) {
                finalValue = (Map) properties.get(separatorKeys[i]);
                continue;
            }
            if (finalValue == null) {
                break;
            }
            finalValue = (Map) finalValue.get(separatorKeys[i]);
        }
        return finalValue == null ? null : finalValue.get(separatorKeys[separatorKeys.length - 1]);
    }
}
