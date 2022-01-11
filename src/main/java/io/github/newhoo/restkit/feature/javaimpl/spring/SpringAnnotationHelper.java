package io.github.newhoo.restkit.feature.javaimpl.spring;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestMethodAnnotation.REQUEST_MAPPING;

public class SpringAnnotationHelper {

    /**
     * 类上的注解
     *
     * @param psiClass
     */
    public static List<MethodPath> getTypeMethodPaths(PsiClass psiClass) {
        // 只能出现 @RequestMapping
        PsiAnnotation requestMappingAnnotation = PsiAnnotationHelper.getInheritedAnnotation(psiClass, REQUEST_MAPPING.getQualifiedName());

        if (requestMappingAnnotation != null) {
            return getMethodPaths(requestMappingAnnotation, REQUEST_MAPPING);
        }

        return Collections.emptyList();
    }

    /**
     * 方法上的注解
     *
     * @param psiMethod
     */
    public static List<MethodPath> getMethodMethodPaths(PsiMethod psiMethod) {
        //方法上只有一个Mapping注解生效，RequestMapping优先级高，其次按出现顺序
        PsiAnnotation requestMappingAnno = psiMethod.getAnnotation(REQUEST_MAPPING.getQualifiedName());
        if (requestMappingAnno != null) {
            return getMethodPaths(requestMappingAnno, REQUEST_MAPPING);
        }
        return Arrays.stream(psiMethod.getAnnotations())
                     .filter(anno -> SpringRequestMethodAnnotation.getByQualifiedName(anno.getQualifiedName()) != null)
                     .findFirst()
                     .map(anno -> getMethodPaths(anno, SpringRequestMethodAnnotation.getByQualifiedName(anno.getQualifiedName())))
                     .orElse(Collections.emptyList());
    }

    /**
     * @param annotation
     */
    private static List<MethodPath> getMethodPaths(@NotNull PsiAnnotation annotation, @NotNull SpringRequestMethodAnnotation mappingAnnotation) {
        List<String> methodList;
        if (mappingAnnotation.getMethod() != null) {
            methodList = Collections.singletonList(mappingAnnotation.getMethod());
        } else { // RequestMapping 如果没有指定具体method，不写的话，默认支持所有HTTP请求方法
            methodList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "method")
                                            .stream()
                                            .map(method -> method.replace("RequestMethod.", ""))
                                            .collect(Collectors.toList());
        }

        List<String> pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "value");
        if (pathList.isEmpty()) {
            pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path");
        }

        // 没有设置 value
        if (pathList.isEmpty()) {
            pathList.add("");
        }

        List<MethodPath> mappingList = new ArrayList<>(4);
        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    mappingList.add(new MethodPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                mappingList.add(new MethodPath(path, null));
            }
        }

        return mappingList;
    }
}