package io.github.newhoo.restkit.feature.javaimpl.helper;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import io.github.newhoo.restkit.util.JsonUtils;
import io.github.newhoo.restkit.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PsiClassHelper in Java
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class PsiClassHelper {

    private static final int MAX_CORRELATION_COUNT = 6;

    public static String convertClassToJSON(String className, Project project) {
        Object o = assemblePsiClass(className, project, 0, false);
        return JsonUtils.toJson(o);
    }

    public static Object assemblePsiClass(String typeCanonicalText, Project project, int autoCorrelationCount, boolean putClass) {
        if (StringUtils.isEmpty(typeCanonicalText)) {
            return "";
        }
        boolean containsGeneric = typeCanonicalText.contains("<");
        // 数组|集合
        if (TypeUtils.isArray(typeCanonicalText) || TypeUtils.isList(typeCanonicalText)) {
            String elementType = TypeUtils.isArray(typeCanonicalText)
                    ? typeCanonicalText.replace("[]", "")
                    : containsGeneric
                    ? typeCanonicalText.substring(typeCanonicalText.indexOf("<") + 1, typeCanonicalText.lastIndexOf(">"))
                    : Object.class.getCanonicalName();
            return Collections.singletonList(assemblePsiClass(elementType, project, autoCorrelationCount, putClass));
        }

        PsiClass psiClass = PsiClassHelper.findPsiClass(typeCanonicalText, project);
        if (psiClass == null) {
            //简单常用类型
            if (TypeUtils.isPrimitiveOrSimpleType(typeCanonicalText)) {
                return TypeUtils.getExampleValue(typeCanonicalText, project);
            }
            return Collections.emptyMap();
        }

        //简单常用类型
        if (TypeUtils.isPrimitiveOrSimpleType(typeCanonicalText)) {
            return TypeUtils.getExampleValue(typeCanonicalText, project);
        }

        // 枚举
        if (psiClass.isEnum()) {
            PsiField[] enumFields = psiClass.getFields();
            for (PsiField enumField : enumFields) {
                if (enumField instanceof PsiEnumConstant) {
                    return enumField.getName();
                }
            }
            return "";
        }

        // Map
        if (TypeUtils.isMap(typeCanonicalText)) {
            return Collections.emptyMap();
        }

        if (autoCorrelationCount > MAX_CORRELATION_COUNT) {
            return Collections.emptyMap();
        }
        autoCorrelationCount++;

        Map<String, Object> map = new LinkedHashMap<>();
        if (putClass) {
            if (containsGeneric) {
                map.put("class", typeCanonicalText.substring(0, typeCanonicalText.indexOf("<")));
            } else {
                map.put("class", typeCanonicalText);
            }
        }
        for (PsiField field : psiClass.getAllFields()) {
            if (field.hasModifierProperty(PsiModifier.STATIC) || field.hasModifierProperty(PsiModifier.FINAL) || field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }
            String fieldType = field.getType().getCanonicalText();
            // 不存在泛型
            if (!containsGeneric) {
                map.put(field.getName(), assemblePsiClass(fieldType, project, autoCorrelationCount, putClass));
                continue;
            }
            // 存在泛型
            if (TypeUtils.isPrimitiveOrSimpleType(fieldType.replaceAll("\\[]", ""))) {
                map.put(field.getName(), assemblePsiClass(fieldType, project, autoCorrelationCount, putClass));
            } else if (PsiClassHelper.findPsiClass(fieldType, project) == null) {
                map.put(field.getName(), assemblePsiClass(typeCanonicalText.substring(typeCanonicalText.indexOf("<") + 1, typeCanonicalText.lastIndexOf(">")), project, autoCorrelationCount, putClass));
            } else {
                map.put(field.getName(), assemblePsiClass(fieldType, project, autoCorrelationCount, putClass));
            }
        }
        return map;
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project           当前project
     * @return 查找到的类
     */
    public static PsiClass findPsiClass(String typeCanonicalText, Project project) {
        // 基本类型转化为对应包装类型
        typeCanonicalText = TypeUtils.primitiveToBox(typeCanonicalText);

        String className = typeCanonicalText;
        if (className.contains("[]")) {
            className = className.replaceAll("\\[]", "");
        }
        if (className.contains("<")) {
            className = className.substring(0, className.indexOf("<"));
        }
        if (className.lastIndexOf(".") > 0) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        PsiClass[] classesByName = PsiShortNamesCache.getInstance(project).getClassesByName(className, GlobalSearchScope.allScope(project));
        for (PsiClass psiClass : classesByName) {
            if (psiClass.getQualifiedName() != null && typeCanonicalText.startsWith(psiClass.getQualifiedName())) {
                return psiClass;
            }
        }
        return null;
    }
}