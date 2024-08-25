package io.github.newhoo.restkit.feature.javaimpl.jaxrs;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.config.JavaFilterSetting;
import io.github.newhoo.restkit.intellij.KtCompactHelper;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.psi.KtAnnotationEntry;
import org.jetbrains.kotlin.psi.KtClass;
import org.jetbrains.kotlin.psi.KtNamedFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jax-RS api Kotlin Resolver, will work when Kotlin enabled
 *
 * @since 3.3.0
 */
public class JaxrsKotlinJavaResolver extends JaxrsJavaResolver {

    @Override
    public List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope) {
        List<RestItem> itemList = new ArrayList<>();
        // 标注了 jax-rs Path 注解的类
        Set<String> filterClassQualifiedNames = JavaFilterSetting.getInstance().getFilterClassQualifiedNames();
        Collection<KtAnnotationEntry> ktAnnotationEntries = KtCompactHelper.getAnnotations(JaxrsAnnotation.PathAnnotation.PATH.getShortName(), module.getProject(), globalSearchScope);
        for (KtAnnotationEntry ktAnnotationEntry : ktAnnotationEntries) {
            PsiAnnotation classAnnotation = LightClassUtilsKt.toLightAnnotation(ktAnnotationEntry);
            if (classAnnotation == null) {
                continue;
            }
            if (!JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName().equals(classAnnotation.getQualifiedName()) && !JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName2().equals(classAnnotation.getQualifiedName())) {
                continue;
            }
            PsiElement psiElement = ktAnnotationEntry.getParent().getParent();
            if (psiElement instanceof KtClass) {
                KtClass ktClass = (KtClass) psiElement;
                FqName fqName = ktClass.getFqName();
                if (fqName != null && filterClassQualifiedNames.contains(fqName.asString())) {
                    continue;
                }
                List<KtNamedFunction> ktNamedFunctions = ktClass.getDeclarations().stream()
                                                                .filter(declaration -> declaration instanceof KtNamedFunction)
                                                                .map(declaration -> (KtNamedFunction) declaration)
                                                                .collect(Collectors.toList());
                if (ktNamedFunctions.isEmpty()) {
                    return Collections.emptyList();
                }

                String classUriPath = getTypeMethodPaths(ktClass);
                for (KtNamedFunction fun : ktNamedFunctions) {
                    MethodPath methodMethodPath = getMethodMethodPath(fun);
                    if (methodMethodPath != null) {
                        String requestPath = RequestHelper.getCombinedPath(classUriPath, methodMethodPath.getPath());
                        RestItem item = new PsiRestItem(requestPath, methodMethodPath.getMethod(), module.getName(), getFrameworkName(), fun, this);
                        item.setPackageName(fqName != null ? fqName.asString() : fun.getContainingKtFile().getName().replace(".kt", ""));
                        itemList.add(item);
                    }
                }
            }
        }
        return itemList;
    }

    private String getTypeMethodPaths(KtClass ktClass) {
        if (ktClass.getModifierList() == null) {
            return "";
        }
        for (KtAnnotationEntry entry : ktClass.getModifierList().getAnnotationEntries()) {
            PsiAnnotation psiAnnotation = LightClassUtilsKt.toLightAnnotation(entry);
            if (psiAnnotation != null) {
                if (JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName().equals(psiAnnotation.getQualifiedName()) || JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName2().equals(psiAnnotation.getQualifiedName())) {
//                    return getAttributeValues(entry, "value");
                    return PsiAnnotationHelper.getAnnotationValue(psiAnnotation, "value");
                }
            }
        }
        return "";
    }

    MethodPath getMethodMethodPath(KtNamedFunction fun) {
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(fun);
        if (!psiMethods.isEmpty()) {
            return getMethodPath(psiMethods.get(0));
        }
//        if (fun.getModifierList() == null) {
//            return null;
//        }
//        String methodPath = "";
//        String methodMethod = "";
//        for (KtAnnotationEntry entry : fun.getModifierList().getAnnotationEntries()) {
//            if (entry.getShortName() != null) {
//                String annotationName = entry.getShortName().asString();
//                if (JaxrsAnnotation.PathAnnotation.PATH.getShortName().equals(annotationName)) {
//                    methodPath = getAttributeValues(entry, "value");
//                } else {
//                    for (JaxrsAnnotation.MethodAnnotation methodAnnotation : JaxrsAnnotation.MethodAnnotation.values()) {
//                        if (methodAnnotation.name().equals(annotationName)) {
//                            methodMethod = methodAnnotation.name();
//                        }
//                    }
//                }
//            }
//        }
//        if (!methodMethod.isEmpty()) {
//            return new MethodPath(methodPath, methodMethod);
//        }
        return null;
    }

//    private String getAttributeValues(KtAnnotationEntry entry, String attribute) {
//        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();
//        if (valueArgumentList == null) {
//            return "";
//        }
//        List<KtValueArgument> arguments = valueArgumentList.getArguments();
//        for (KtValueArgument ktValueArgument : arguments) {
//            KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
//            if (argumentExpression == null) {
//                continue;
//            }
//            KtValueArgumentName argumentName = ktValueArgument.getArgumentName();
//            if (argumentName == null) {
//                String text = ktValueArgument.getText();
//                return text.length() > 2 ? text.substring(1, text.length() - 1) : text;
//            }
//            if (argumentName.getText().equals(attribute)) {
//                // 有且仅有一个value
//                PsiElement[] paths = argumentExpression.getChildren();
//                return paths.length == 0 ? "" : paths[0].getText();
//            }
//        }
//        return "";
//    }

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
//        String classUriPath = getTypeMethodPaths((KtClass) function.getParent().getParent());
//        MethodPath methodMethodPath = getMethodMethodPaths(function);
//        if (methodMethodPath != null) {
//            String requestPath = RequestHelper.getCombinedPath(classUriPath, methodMethodPath.getPath());
//            return new PsiRestItem(requestPath, methodMethodPath.getMethod(), "", getFrameworkName(), function, this);
//        }
//        return null;
//        if (!(psiElement instanceof KtNamedFunction)) {
//            return null;
//        }
//        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(function);
        if (!psiMethods.isEmpty()) {
            return super.tryGenerateRestItem(psiMethods.get(0));
        }
        return null;
    }

    @Override
    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
//        return psiElement instanceof KtNamedFunction
//                && ((KtNamedFunction) psiElement).getAnnotationEntries()
//                                                 .stream()
//                                                 .filter(ktAnnotationEntry -> ktAnnotationEntry.getShortName() != null)
//                                                 .anyMatch(ktAnnotationEntry -> JaxrsAnnotation.MethodAnnotation.valueOf(ktAnnotationEntry.getShortName().asString()) != null);
        if (!(psiElement instanceof KtNamedFunction)) {
            return false;
        }
//        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
        if (!psiMethods.isEmpty()) {
            return super.canNavigateToTree(psiMethods.get(0));
        }
        return false;
    }

    @Override
    public boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return "fun".equals(psiElement.getText())
                && canNavigateToTree(psiElement.getParent());
//        if (!(psiElement instanceof KtNamedFunction)) {
//            return false;
//        }
//        KtNamedFunction ktNamedFunction = (KtNamedFunction) psiElement;
//        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
//        if (!psiMethods.isEmpty()) {
//            return super.canGenerateLineMarker(psiMethods.get(0));
//        }
//        return false;
    }

    @Override
    public @NotNull List<KV> buildHeaders(@NotNull PsiElement psiElement) {
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
        if (!psiMethods.isEmpty()) {
            return super.buildHeaders(psiMethods.get(0));
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<KV> buildParams(@NotNull PsiElement psiElement) {
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
        if (!psiMethods.isEmpty()) {
            return super.buildParams(psiMethods.get(0));
        }
        return Collections.emptyList();
    }

    @Override
    public @NotNull String buildRequestBodyJson(@NotNull PsiElement psiElement) {
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
        if (!psiMethods.isEmpty()) {
            return super.buildRequestBodyJson(psiMethods.get(0));
        }
        return "";
    }

    @Override
    public @NotNull String buildDescription(@NotNull PsiElement psiElement) {
        List<PsiMethod> psiMethods = LightClassUtilsKt.toLightMethods(psiElement);
        if (!psiMethods.isEmpty()) {
            return super.buildDescription(psiMethods.get(0));
        }
        return "";
    }

    public static class JaxrsKotlinResolverProvider implements RestfulResolverProvider {

        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new JaxrsKotlinJavaResolver();
        }
    }
}
