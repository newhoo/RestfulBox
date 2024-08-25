package io.github.newhoo.restkit.feature.javaimpl.spring;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.config.JavaFilterSetting;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiClassHelper;
import io.github.newhoo.restkit.restful.BaseRequestResolver;
import io.github.newhoo.restkit.restful.ParamResolver;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.util.TypeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.common.RestConstant.HTTP_FILE_DEFAULT_DESCRIPTOR;
import static io.github.newhoo.restkit.common.RestConstant.WEB_FRAMEWORK_SPRING_MVC;
import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestMethodAnnotation.REQUEST_MAPPING;
import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestParamAnnotation.PATH_VARIABLE;
import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestParamAnnotation.REQUEST_BODY;
import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestParamAnnotation.REQUEST_HEADER;
import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestParamAnnotation.REQUEST_PARAM;

/**
 * Base spring resolver for SpringRequestResolver in Java and kotlin
 *
 * @author huzunrong
 * @since 2.0.0
 */
public abstract class BaseSpringResolver extends BaseRequestResolver implements ParamResolver {

    @NotNull
    @Override
    public String getFrameworkName() {
        return WEB_FRAMEWORK_SPRING_MVC;
    }

    @NotNull
    public RestItem createRestServiceItem(@NotNull Module module, PsiElement psiElement, @NotNull String typePath, @NotNull String methodPath, String method) {
        String requestPath = RequestHelper.getCombinedPath(typePath, methodPath);
        return new PsiRestItem(requestPath, method, module.getName(), getFrameworkName(), psiElement, this);
    }

    @NotNull
    public RestItem createRestServiceItem(@NotNull Module module, PsiElement psiElement, @NotNull String path, String method) {
        return new PsiRestItem(path, method, module.getName(), getFrameworkName(), psiElement, this);
    }

    public List<RestItem> combineTypeAndMethod(List<MethodPath> typeMethodPaths, List<MethodPath> methodMethodPaths, PsiElement psiElement, Module module) {
        List<RestItem> itemList = new ArrayList<>();
        for (MethodPath methodPath : methodMethodPaths) {
            if (typeMethodPaths.isEmpty()) {
                RestItem item = createRestServiceItem(module, psiElement, "", methodPath.getPath(), methodPath.getMethod());
                itemList.add(item);
            } else {
                for (MethodPath typeMethodPath : typeMethodPaths) {
                    String combinedPath = RequestHelper.getCombinedPath(typeMethodPath.getPath(), methodPath.getPath());
                    String typeMethod = typeMethodPath.getMethod();

                    if (typeMethod != null && !typeMethod.equals(methodPath.getMethod())) {
                        RestItem item = createRestServiceItem(module, psiElement, combinedPath, typeMethod);
                        itemList.add(item);
                    }

                    RestItem item = createRestServiceItem(module, psiElement, combinedPath, methodPath.getMethod());
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    public RestItem combineFirstRestItem(List<MethodPath> typeMethodPaths, List<MethodPath> methodMethodPaths, PsiElement psiElement, String moduleName) {
        if (methodMethodPaths.isEmpty()) {
            return null;
        }
        MethodPath methodPath = methodMethodPaths.get(0);
        if (typeMethodPaths.isEmpty()) {
            String requestPath = RequestHelper.getCombinedPath("", methodPath.getPath());
            return new PsiRestItem(requestPath, methodPath.getMethod(), moduleName, getFrameworkName(), psiElement, this);
        } else {
            MethodPath typeMethodPath = typeMethodPaths.get(0);
            String combinedPath = RequestHelper.getCombinedPath(typeMethodPath.getPath(), methodPath.getPath());
            String typeMethod = typeMethodPath.getMethod();

            if (typeMethod != null && !typeMethod.equals(methodPath.getMethod())) {
                return new PsiRestItem(combinedPath, typeMethod, moduleName, getFrameworkName(), psiElement, this);
            }

            return new PsiRestItem(combinedPath, methodPath.getMethod(), moduleName, getFrameworkName(), psiElement, this);
        }
    }

    public List<KV> buildHeaderString(PsiMethod psiMethod) {
        List<KV> list = new ArrayList<>();
        final PsiAnnotation controllerAnno = PsiAnnotationHelper.getInheritedAnnotation(psiMethod.getContainingClass(), REQUEST_MAPPING.getQualifiedName());
        if (controllerAnno != null) {
            final PsiAnnotationMemberValue headers = controllerAnno.findAttributeValue("headers");
            list.addAll(getHeaderItem(headers));
        }
        for (SpringRequestMethodAnnotation value : SpringRequestMethodAnnotation.values()) {
            final PsiAnnotation anno = PsiAnnotationHelper.getInheritedAnnotation(psiMethod, value.getQualifiedName());
            if (anno != null) {
                final PsiAnnotationMemberValue methodHeaders = anno.findAttributeValue("headers");
                list.addAll(getHeaderItem(methodHeaders));
                break;
            }
        }
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            // @PathVariable
            PsiAnnotation requestHeaderAnno = psiParameter.getAnnotation(REQUEST_HEADER.getQualifiedName());
            if (requestHeaderAnno != null) {
                String headerName = StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(requestHeaderAnno, "value"),
                        StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(requestHeaderAnno, "name"), psiParameter.getName()
                        ));
                PsiClass fieldClass = PsiClassHelper.findPsiClass(psiParameter.getType().getCanonicalText(), psiMethod.getProject());
                if (fieldClass != null && fieldClass.isEnum()) {
                    PsiField[] enumFields = fieldClass.getAllFields();
                    list.add(new KV(headerName, enumFields.length > 1 ? enumFields[0].getName() : ""));
                } else {
                    Object fieldDefaultValue = TypeUtils.getExampleValue(psiParameter.getType().getPresentableText(), psiParameter.getProject());
                    list.add(new KV(headerName, String.valueOf(fieldDefaultValue)));
                }
            }
        }
        return list;
    }

    public List<KV> buildParamString(PsiMethod psiMethod) {
        List<KV> list = new ArrayList<>();

        List<Parameter> parameterList = getParameterList(psiMethod);

        // 拼接参数
        for (Parameter parameter : parameterList) {
            String paramType = parameter.getParamType();

            // 数组|集合
            if (TypeUtils.isArray(paramType) || TypeUtils.isList(paramType)) {
                paramType = TypeUtils.isArray(paramType)
                        ? paramType.replace("[]", "")
                        : paramType.contains("<")
                        ? paramType.substring(paramType.indexOf("<") + 1, paramType.lastIndexOf(">"))
                        : Object.class.getCanonicalName();
            }

            // 简单常用类型
            if (TypeUtils.isPrimitiveOrSimpleType(paramType)) {
                list.add(new KV(parameter.getParamName(), String.valueOf(TypeUtils.getExampleValue(paramType, psiMethod.getProject()))));
                continue;
            }
            // 文件类型
            Set<String> fileParameterTypeSet = JavaFilterSetting.getInstance().getFileParameterQualifiedNames();
            if (fileParameterTypeSet.contains(paramType)) {
                list.add(new KV(parameter.getParamName(), HTTP_FILE_DEFAULT_DESCRIPTOR));
                continue;
            }

            PsiClass psiClass = PsiClassHelper.findPsiClass(paramType, psiMethod.getProject());
            if (psiClass != null) {
                PsiField[] fields = psiClass.getAllFields();
                if (psiClass.isEnum()) {
                    list.add(new KV(parameter.getParamName(), fields.length > 1 ? fields[0].getName() : ""));
                    continue;
                }
                for (PsiField field : fields) {
                    if (field.hasModifierProperty(PsiModifier.STATIC) || field.hasModifierProperty(PsiModifier.TRANSIENT)) {
                        continue;
                    }
                    PsiClass fieldClass = PsiClassHelper.findPsiClass(field.getType().getCanonicalText(), psiMethod.getProject());
                    if (fieldClass != null && fieldClass.isEnum()) {
                        PsiField[] enumFields = fieldClass.getAllFields();
                        list.add(new KV(field.getName(), enumFields.length > 1 ? enumFields[0].getName() : ""));
                    } else {
                        Object fieldDefaultValue = TypeUtils.getExampleValue(field.getType().getPresentableText(), psiMethod.getProject());
                        list.add(new KV(field.getName(), String.valueOf(fieldDefaultValue)));
                    }
                }
            }
        }
        return list;
    }

    /**
     * 构建RequestBody json 参数
     */
    public String buildRequestBodyJson(PsiMethod psiMethod) {
        return Arrays.stream(psiMethod.getParameterList().getParameters())
                     .filter(psiParameter -> psiParameter.hasAnnotation(REQUEST_BODY.getQualifiedName()))
                     .findFirst()
                     .map(psiParameter -> PsiClassHelper.convertClassToJSON(psiParameter.getType().getCanonicalText(), psiMethod.getProject()))
                     .orElse(null);
    }

    private List<KV> getHeaderItem(PsiAnnotationMemberValue headers) {
        if (headers instanceof PsiLiteralExpression) {
            final String s = String.valueOf(((PsiLiteralExpression) headers).getValue());
            String[] split = StringUtils.split(s, "=");
            return split.length > 1 ? Collections.singletonList(new KV(split[0], split[1])) : Collections.emptyList();
        }

        List<KV> list = new ArrayList<>();
        if (headers instanceof PsiArrayInitializerMemberValue) {
            for (PsiAnnotationMemberValue initializer : ((PsiArrayInitializerMemberValue) headers).getInitializers()) {
                list.addAll(getHeaderItem(initializer));
            }
        }
        return list;
    }

    @NotNull
    private List<Parameter> getParameterList(PsiMethod psiMethod) {
        List<Parameter> parameterList = new ArrayList<>();

        Set<String> paramFilterTypes = getParamFilterTypes(psiMethod.getProject());

        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            String paramTypeName = psiParameter.getType().getCanonicalText();
            if (paramFilterTypes.contains(paramTypeName)
                    || CollectionUtils.containsAny(paramFilterTypes, Arrays.stream(psiParameter.getAnnotations()).map(PsiAnnotation::getQualifiedName).collect(Collectors.toSet()))) {
                continue;
            }

            // @PathVariable
            PsiAnnotation pathVariableAnno = psiParameter.getAnnotation(PATH_VARIABLE.getQualifiedName());
            if (pathVariableAnno != null) {
                String paramName = StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(pathVariableAnno, "value"),
                        StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(pathVariableAnno, "name"), psiParameter.getName()
                        ));
                Parameter parameter = new Parameter(paramTypeName, paramName);
                parameterList.add(parameter);
                continue;
            }

            // @RequestParam
            PsiAnnotation requestParamAnno = psiParameter.getAnnotation(REQUEST_PARAM.getQualifiedName());
            if (requestParamAnno != null) {
                String paramName = StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(requestParamAnno, "value"),
                        StringUtils.defaultIfEmpty(PsiAnnotationHelper.getAnnotationValue(requestParamAnno, "name"), psiParameter.getName()
                        ));
                Parameter parameter = new Parameter(paramTypeName, paramName);
                parameterList.add(parameter);
                continue;
            }

            // 其他未包含指定注解
            Parameter parameter = new Parameter(paramTypeName, psiParameter.getName());
            parameterList.add(parameter);
        }
        return parameterList;
    }

    @NotNull
    @Override
    public Set<String> getParamFilterTypes(@NotNull Project project) {
        return JavaFilterSetting.getInstance().getFilterParamQualifiedNames();
    }

    @Getter
    @AllArgsConstructor
    static class Parameter {
        private String paramType;
        private String paramName;
    }
}
