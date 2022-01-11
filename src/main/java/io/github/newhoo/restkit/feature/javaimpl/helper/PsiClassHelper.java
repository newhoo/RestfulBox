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

    private static final int MAX_CORRELATION_COUNT = 5;

    public static String convertClassToJSON(String className, Project project) {
        Object o = assemblePsiClass(className, project, 0);
        return JsonUtils.toJson(o);
    }

    private static Object assemblePsiClass(String typeCanonicalText, Project project, int autoCorrelationCount) {
        PsiClass psiClass = findPsiClass(typeCanonicalText, project);
        if (psiClass == null) {
            return Collections.emptyMap();
        }

        //简单常用类型
        if (TypeUtils.isPrimitiveOrSimpleType(typeCanonicalText)) {
            return TypeUtils.getExampleValue(typeCanonicalText, false);
        }

        // 数组|集合
        if (TypeUtils.isArray(typeCanonicalText) || TypeUtils.isList(typeCanonicalText)) {
            String elementType = TypeUtils.isArray(typeCanonicalText)
                    ? typeCanonicalText.replace("[]", "")
                    : typeCanonicalText.contains("<")
                    ? typeCanonicalText.substring(typeCanonicalText.indexOf("<") + 1, typeCanonicalText.lastIndexOf(">"))
                    : Object.class.getCanonicalName();
            return Collections.singletonList(assemblePsiClass(elementType, project, autoCorrelationCount));
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
        for (PsiField field : psiClass.getAllFields()) {
            if (field.hasModifierProperty(PsiModifier.STATIC) || field.hasModifierProperty(PsiModifier.FINAL) || field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                continue;
            }
            map.put(field.getName(), assemblePsiClass(field.getType().getCanonicalText(), project, autoCorrelationCount));
        }
        return map;
    }

    /**
     * 查找类
     *
     * @param typeCanonicalText 参数类型全限定名称
     * @param project 当前project
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
            if (typeCanonicalText.startsWith(psiClass.getQualifiedName())) {
                return psiClass;
            }
        }
        return null;
    }
}