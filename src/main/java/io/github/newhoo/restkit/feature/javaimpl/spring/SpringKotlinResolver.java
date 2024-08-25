package io.github.newhoo.restkit.feature.javaimpl.spring;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.config.JavaFilterSetting;
import io.github.newhoo.restkit.intellij.KtCompactHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtCollectionLiteralExpression;
import org.jetbrains.kotlin.psi.KtExpression;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtValueArgument;
import org.jetbrains.kotlin.psi.KtValueArgumentList;
import org.jetbrains.kotlin.psi.KtValueArgumentName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestMethodAnnotation.REQUEST_MAPPING;

/**
 * Spring api Kotlin Resolver, will work when Kotlin enabled
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class SpringKotlinResolver extends BaseSpringResolver {

    @Override
    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return psiElement instanceof KtNamedFunction
                && ((KtNamedFunction) psiElement).getAnnotationEntries()
                                                 .stream()
                                                 .filter(ktAnnotationEntry -> ktAnnotationEntry.getShortName() != null)
                                                 .anyMatch(ktAnnotationEntry -> SpringRequestMethodAnnotation.getByShortName(ktAnnotationEntry.getShortName().asString()) != null);
    }

    @Override
    public boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return "fun".equals(psiElement.getText())
                && canNavigateToTree(psiElement.getParent());
    }

    @Override
    public RestItem tryGenerateRestItem(@NotNull PsiElement psiElement) {
        KtNamedFunction function;
        if (psiElement instanceof KtNamedFunction) {
            function = (KtNamedFunction) psiElement;
        } else if (psiElement.getParent() instanceof KtNamedFunction) {
            function = (KtNamedFunction) psiElement.getParent();
        } else {
            return null;
        }
        if (function.getParent() == null || function.getParent().getParent() == null) {
            return null;
        }
        List<MethodPath> typeMethodPaths = getTypeMethodPaths((KtClass) function.getParent().getParent());
        List<MethodPath> methodMethodPaths = getMethodMethodPaths(function);
        return combineFirstRestItem(typeMethodPaths, methodMethodPaths, function, "");
    }

    @Override
    public List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope) {
        List<RestItem> itemList = new ArrayList<>();
        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        Set<String> filterClassQualifiedNames = JavaFilterSetting.getInstance().getFilterClassQualifiedNames();
        for (SpringControllerAnnotation controllerAnnotation : supportedAnnotations) {
            // kotlin:
            Collection<KtAnnotationEntry> ktAnnotationEntries = KtCompactHelper.getAnnotations(controllerAnnotation.getShortName(), module.getProject(), globalSearchScope);
            for (KtAnnotationEntry ktAnnotationEntry : ktAnnotationEntries) {
                PsiElement psiElement = ktAnnotationEntry.getParent().getParent();

                if (psiElement instanceof KtClass) {
                    KtClass ktClass = (KtClass) psiElement;
                    FqName fqName = ktClass.getFqName();
                    if (fqName != null && filterClassQualifiedNames.contains(fqName.asString())) {
                        continue;
                    }
                    List<RestItem> serviceItemList = getRequestItemList(ktClass, module);
                    itemList.addAll(serviceItemList);
                }
            }
        }
        return itemList;
    }

    private List<RestItem> getRequestItemList(KtClass ktClass, Module module) {
        List<KtNamedFunction> ktNamedFunctions = ktClass.getDeclarations().stream()
                                                        .filter(declaration -> declaration instanceof KtNamedFunction)
                                                        .map(declaration -> (KtNamedFunction) declaration)
                                                        .collect(Collectors.toList());
        if (ktNamedFunctions.isEmpty()) {
            return Collections.emptyList();
        }

        List<RestItem> itemList = new ArrayList<>();
        List<MethodPath> typeMethodPaths = getTypeMethodPaths(ktClass);

        for (KtNamedFunction fun : ktNamedFunctions) {

            List<MethodPath> methodMethodPaths = getMethodMethodPaths(fun);
            List<RestItem> restItems = combineTypeAndMethod(typeMethodPaths, methodMethodPaths, fun, module);
            PsiClass[] psiClasses = fun.getContainingKtFile().getClasses();
            restItems.forEach(e -> e.setPackageName(psiClasses.length > 0 ? psiClasses[0].getQualifiedName() : fun.getContainingKtFile().getName().replace(".kt", "")));
            itemList.addAll(restItems);
        }
        return itemList;
    }

    private List<MethodPath> getTypeMethodPaths(KtClass ktClass) {
        // 只能出现 @RequestMapping
        if (ktClass.getModifierList() == null) {
            return Collections.emptyList();
        }
        for (KtAnnotationEntry entry : ktClass.getModifierList().getAnnotationEntries()) {
            if (entry.getShortName() != null) {
                String annotationName = entry.getShortName().asString();
                if (REQUEST_MAPPING.getShortName().equals(annotationName)) {
                    return getMethodPaths(entry, REQUEST_MAPPING);
                }
            }
        }
        return Collections.emptyList();
    }

    List<MethodPath> getMethodMethodPaths(KtNamedFunction fun) {
        //方法上只有一个Mapping注解生效，RequestMapping优先级高，其次按出现顺序
        if (fun.getModifierList() == null) {
            return Collections.emptyList();
        }
        for (KtAnnotationEntry entry : fun.getModifierList().getAnnotationEntries()) {
            if (entry.getShortName() != null) {
                String annotationName = entry.getShortName().asString();
                if (REQUEST_MAPPING.getShortName().equals(annotationName)) {
                    return getMethodPaths(entry, REQUEST_MAPPING);
                }

                SpringRequestMethodAnnotation requestMethodAnnotation = SpringRequestMethodAnnotation.getByShortName(annotationName);
                if (requestMethodAnnotation != null) {
                    return getMethodPaths(entry, requestMethodAnnotation);
                }

            }
        }
        return Collections.emptyList();
    }

    private List<MethodPath> getMethodPaths(@NotNull KtAnnotationEntry entry, @NotNull SpringRequestMethodAnnotation mappingAnnotation) {
        List<String> methodList;
        if (mappingAnnotation.getMethod() != null) {
            methodList = Collections.singletonList(mappingAnnotation.getMethod());
        } else { // RequestMapping 如果没有指定具体method，不写的话，默认支持所有HTTP请求方法
            methodList = getAttributeValues(entry, "method")
                    .stream()
                    .map(method -> method.replace("RequestMethod.", ""))
                    .collect(Collectors.toList());
        }

        List<String> pathList = new ArrayList<>();

        //注解参数值
        List<String> mappingValues = getAttributeValues(entry, null);
        if (!mappingValues.isEmpty()) {
            pathList.addAll(mappingValues);
        } else {
            pathList.addAll(getAttributeValues(entry, "value"));
        }

        pathList.addAll(getAttributeValues(entry, "path"));

        // 没有设置 value，默认方法名
        if (pathList.isEmpty()) {
            pathList.add("");
        }

        List<MethodPath> methodPaths = new ArrayList<>(4);
        if (!methodList.isEmpty()) {
            for (String method : methodList) {
                for (String path : pathList) {
                    methodPaths.add(new MethodPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                methodPaths.add(new MethodPath(path, null));
            }
        }

        return methodPaths;
    }

    private List<String> getAttributeValues(KtAnnotationEntry entry, String attribute) {
        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();

        if (valueArgumentList == null) {
            return Collections.emptyList();
        }

        List<KtValueArgument> arguments = valueArgumentList.getArguments();

        for (KtValueArgument ktValueArgument : arguments) {
            KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
            if (argumentExpression == null) {
                continue;
            }

            KtValueArgumentName argumentName = ktValueArgument.getArgumentName();
            if ((argumentName == null && attribute == null) || (argumentName != null && argumentName.getText().equals(attribute))) {
                List<String> methodList = new ArrayList<>();

                // array, kotlin 1.1-
                if (argumentExpression.getText().startsWith("arrayOf")) {
                    List<KtValueArgument> pathValueArguments = ((KtCallExpression) argumentExpression).getValueArguments();
                    for (KtValueArgument pathValueArgument : pathValueArguments) {
                        methodList.add(pathValueArgument.getText().replace("\"", ""));
                    }
                    // array, kotlin 1.2+
                } else if (argumentExpression.getText().startsWith("[")) {
                    List<KtExpression> innerExpressions = ((KtCollectionLiteralExpression) argumentExpression).getInnerExpressions();
                    for (KtExpression ktExpression : innerExpressions) {
                        methodList.add(ktExpression.getText().replace("\"", ""));
                    }
                } else {
                    // 有且仅有一个value
                    PsiElement[] paths = argumentExpression.getChildren();
//                            Arrays.stream(paths).forEach(p -> methodList.add(p.getText()));
                    methodList.add(paths.length == 0 ? "" : paths[0].getText());
                }

                return methodList;
            }
        }

        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<KV> buildHeaders(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof KtNamedFunction)) {
            return Collections.emptyList();
        }
        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(ktNamedFunction);
        return buildHeaderString(psiMethods.get(0));
    }

    @NotNull
    @Override
    public List<KV> buildParams(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof KtNamedFunction)) {
            return Collections.emptyList();
        }
        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(ktNamedFunction);
        PsiMethod psiMethod = psiMethods.get(0);
        return buildParamString(psiMethod);
    }

    @NotNull
    @Override
    public String buildRequestBodyJson(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof KtNamedFunction)) {
            return "";
        }
        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(ktNamedFunction);
        PsiMethod psiMethod = psiMethods.get(0);
        String s = buildRequestBodyJson(psiMethod);
        return Objects.nonNull(s) ? s : "";
    }

    @NotNull
    @Override
    public String buildDescription(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof KtNamedFunction)) {
            return "";
        }
        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        String location = ktNamedFunction.getName();
        if (StringUtils.isEmpty(location)) {
            return "";
        }

        if (psiElement.getParent() != null && psiElement.getParent().getParent() instanceof KtClass) {
            String className = ((KtClass) psiElement.getParent().getParent()).getName();
            if (StringUtils.isNotEmpty(className)) {
                location = className + "#" + location;
            }
        }
//        String comment = null;
//        if (StringUtils.isNotEmpty(comment)) {
//            location = location.concat("#").concat(comment);
//        }
        return location;
    }

    public static class SpringKotlinRequestResolverProvider implements RestfulResolverProvider {

        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new SpringKotlinResolver();
        }
    }
}