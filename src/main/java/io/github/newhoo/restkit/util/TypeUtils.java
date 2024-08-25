package io.github.newhoo.restkit.util;

import com.intellij.openapi.project.Project;
import io.github.newhoo.restkit.common.NotProguard;
import io.github.newhoo.restkit.config.RequestSetting;
import io.github.newhoo.restkit.datasource.DataSourceHelper;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TypeUtils
 *
 * @author huzunrong
 * @since 1.0.0
 */
@NotProguard
public final class TypeUtils {

    public static boolean isArray(String type) {
        return type.endsWith("[]");
    }

    public static boolean isList(String type) {
        if (type.contains("<")) {
            type = type.substring(0, type.indexOf("<"));
        }
        switch (type) {
            case "java.util.List":
            case "java.util.ArrayList":
            case "java.util.LinkedList":
            case "java.util.Set":
            case "java.util.HashSet":
            case "java.util.TreeSet":
            case "java.util.SortedSet":
            case "java.util.Collection":
            case "java.util.ArrayDeque":
            case "java.util.PriorityQueue":
                return true;
            default:
                return false;
        }
    }

    public static boolean isMap(String type) {
        if (type.contains("<")) {
            type = type.substring(0, type.indexOf("<"));
        }
        switch (type) {
            case "java.util.Properties":
            case "java.util.Map":
            case "java.util.HashMap":
            case "java.util.LinkedHashMap":
            case "java.util.TreeMap":
            case "java.util.SortedMap":
            case "java.util.ConcurrentHashMap":
            case "java.util.Hashtable":
                return true;
            default:
                return false;
        }
    }

    public static boolean isPrimitiveOrSimpleType(String parameterType) {
        if (parameterType == null) {
            return false;
        }
        String type = parameterType.replace("PsiType:", "");
        switch (type) {
            case "byte":
            case "java.lang.Byte":
            case "char":
            case "java.lang.String":
            case "java.util.Date":
            case "java.sql.Timestamp":
            case "java.time.LocalDate":
            case "java.time.LocalDateTime":
            case "short":
            case "java.lang.Short":
            case "int":
            case "java.lang.Integer":
            case "long":
            case "java.lang.Long":
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.Double":
            case "java.math.BigInteger":
            case "java.math.BigDecimal":
            case "boolean":
            case "java.lang.Boolean":
                return true;
            default:
                return false;
        }
    }

    /**
     * 基本类型转化为包装类型
     *
     * @param classType 基本类型
     */
    public static String primitiveToBox(String classType) {
        switch (classType) {
            case "char":
                return "java.lang.Character";
            case "short":
                return "java.lang.Short";
            case "int":
                return "java.lang.Integer";
            case "long":
                return "java.lang.Long";
            case "float":
                return "java.lang.Float";
            case "double":
                return "java.lang.Double";
            case "boolean":
                return "java.lang.Boolean";
            default:
        }
        return classType;
    }

    @NotNull
    public static Object getExampleValue(String parameterType, Project project) {
        if (parameterType.isEmpty()) {
            return "";
        }
        if (parameterType.lastIndexOf(".") > 0) {
            parameterType = parameterType.substring(parameterType.lastIndexOf(".") + 1);
        }
        String type = parameterType.replace("PsiType:", "");
        switch (type) {
            case "byte":
            case "Byte":
                return Byte.valueOf("1");
            case "char":
                return 'a';
            case "String":
                return "";
            case "Date":
            case "Timestamp":
            case "LocalDate":
            case "LocalDateTime":
                RequestSetting setting = DataSourceHelper.getDataSource().selectRequestSetting(project.getName(), project);
                String defaultDateFormat = setting.getDefaultDateFormat();
                if (StringUtils.isEmpty(defaultDateFormat) || "timestamp(ms)".equals(defaultDateFormat)) {
                    return System.currentTimeMillis();
                } else if ("timestamp(s)".equals(defaultDateFormat)) {
                    return System.currentTimeMillis() / 1000;
                }
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(defaultDateFormat);
                    return dateFormat.format(new Date());
                } catch (Exception e) {
                    return System.currentTimeMillis();
                }
            case "short":
            case "Short":
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "BigInteger":
            case "BigDecimal":
                return 0;
            case "float":
            case "Float":
            case "double":
            case "Double":
                return 0.0;
            case "boolean":
            case "Boolean":
                return true;
            default:
                return "";
        }
    }
}
