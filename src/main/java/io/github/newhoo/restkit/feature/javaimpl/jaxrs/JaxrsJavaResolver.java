package io.github.newhoo.restkit.feature.javaimpl.jaxrs;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.config.JavaFilterSetting;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiClassHelper;
import io.github.newhoo.restkit.intellij.JavaCompactHelper;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Jax-RS api Java Resolver, will work when Java enabled
 *
 * @since 3.3.0
 */
public class JaxrsJavaResolver extends BaseJaxrsResolver {

    @Override
    public List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope) {
        // 标注了 jax-rs Path 注解的类
        Collection<PsiAnnotation> psiAnnotations = JavaCompactHelper.getAnnotations(JaxrsAnnotation.PathAnnotation.PATH.getShortName(), module.getProject(), globalSearchScope)
                                                                    .stream()
                                                                    .filter(psiAnnotation -> JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName().equals(psiAnnotation.getQualifiedName()) || JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName2().equals(psiAnnotation.getQualifiedName()))
                                                                    .collect(Collectors.toList());
        List<RestItem> itemList = new ArrayList<>();
        Set<String> filterClassQualifiedNames = JavaFilterSetting.getInstance().getFilterClassQualifiedNames();
        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) {
                continue;
            }
            if (filterClassQualifiedNames.contains(((PsiClass) psiElement).getQualifiedName())) {
                continue;
            }
            String classUriPath = PsiAnnotationHelper.getAnnotationValue(psiAnnotation, "value");

            PsiMethod[] psiMethods = ((PsiClass) psiElement).getMethods();
            for (PsiMethod psiMethod : psiMethods) {
                MethodPath methodPath = getMethodPath(psiMethod);
                if (methodPath != null) {
                    String requestPath = RequestHelper.getCombinedPath(classUriPath, methodPath.getPath());
                    RestItem item = new PsiRestItem(requestPath, methodPath.getMethod(), module.getName(), getFrameworkName(), psiMethod, this);
                    item.setPackageName(((PsiClass) psiElement).getQualifiedName());
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    @Override
    public RestItem tryGenerateRestItem(@NotNull PsiElement psiElement) {
        PsiMethod psiMethod;
        if (psiElement instanceof PsiMethod) {
            psiMethod = (PsiMethod) psiElement;
        } else if (psiElement.getParent() instanceof PsiMethod) {
            psiMethod = (PsiMethod) psiElement.getParent();
        } else {
            return null;
        }
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass == null) {
            return null;
        }
        PsiAnnotation classAnnotation = containingClass.getAnnotation(JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName());
        if (classAnnotation == null) {
            classAnnotation = containingClass.getAnnotation(JaxrsAnnotation.PathAnnotation.PATH.getQualifiedName2());
        }
        String classUriPath = PsiAnnotationHelper.getAnnotationValue(classAnnotation, "value");
        MethodPath methodPath = getMethodPath(psiMethod);
        if (methodPath != null) {
            String requestPath = RequestHelper.getCombinedPath(classUriPath, methodPath.getPath());
            return new PsiRestItem(requestPath, methodPath.getMethod(), "", getFrameworkName(), psiMethod, this);
        }
        return null;
    }

    @Override
    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiMethod
                && Arrays.stream(((PsiMethod) psiElement).getAnnotations())
                         .anyMatch(psiAnnotation -> JaxrsAnnotation.MethodAnnotation.getByQualifiedName(psiAnnotation.getQualifiedName()) != null);
    }

    @Override
    public boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiIdentifier && canNavigateToTree(psiElement.getParent());
    }

    @Override
    public @NotNull List<KV> buildHeaders(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return Collections.emptyList();
        }
        List<KV> list = new ArrayList<>();
        List<KV> cookieKVList = new ArrayList<>();
        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            getParamKv(psiParameter, JaxrsAnnotation.ParamAnnotation.HEADER_PARAM).ifPresent(list::add);
            getParamKv(psiParameter, JaxrsAnnotation.ParamAnnotation.COOKIE_PARAM).ifPresent(cookieKVList::add);
        }
        if (!cookieKVList.isEmpty()) {
            String cookieValue = cookieKVList.stream().map(kv -> kv.getKey() + "=" + kv.getValue()).collect(Collectors.joining("; "));
            list.add(new KV("cookie", cookieValue));
        }
        return list;
    }

    @Override
    public @NotNull List<KV> buildParams(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return Collections.emptyList();
        }
        List<KV> list = new ArrayList<>();
        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            getParamKv(psiParameter, JaxrsAnnotation.ParamAnnotation.PATH_PARAM).ifPresent(list::add);
            getParamKv(psiParameter, JaxrsAnnotation.ParamAnnotation.QUERY_PARAM).ifPresent(list::add);
            getParamKv(psiParameter, JaxrsAnnotation.ParamAnnotation.FORM_PARAM).ifPresent(list::add);
        }
        return list;
    }

    @Override
    public @NotNull String buildRequestBodyJson(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return "";
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        Set<String> paramFilterTypes = JavaFilterSetting.getInstance().getFilterParamQualifiedNames();
        Set<String> paramAnnotationSet = JaxrsAnnotation.ParamAnnotation.getByQualifiedNameSet();
        for (PsiParameter psiParameter : psiParameters) {
            String paramTypeName = psiParameter.getType().getCanonicalText();
            Set<String> annos = Arrays.stream(psiParameter.getAnnotations()).map(PsiAnnotation::getQualifiedName).collect(Collectors.toSet());
            if (paramFilterTypes.contains(paramTypeName) || CollectionUtils.containsAny(paramFilterTypes, annos)
                    || CollectionUtils.containsAny(paramAnnotationSet, annos)) {
                continue;
            }
            return PsiClassHelper.convertClassToJSON(psiParameter.getType().getCanonicalText(), psiMethod.getProject());
        }
        return "";
    }

    @Override
    public @NotNull String buildDescription(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return "";
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;

        String location = psiMethod.getName();
        if (psiMethod.getContainingClass() != null && psiMethod.getContainingClass().getName() != null) {
            location = psiMethod.getContainingClass().getName().concat("#").concat(location);
        }
        if (psiMethod.getDocComment() != null) {
            String comment = Arrays.stream(psiMethod.getDocComment().getDescriptionElements())
                                   .filter(e -> e instanceof PsiDocToken)
                                   .filter(e -> StringUtils.isNotBlank(e.getText()))
                                   .findFirst()
                                   .map(e -> e.getText().trim()).orElse(null);
            if (StringUtils.isNotEmpty(comment)) {
                location = location.concat("#").concat(comment);
            }
        }
        return location;
    }

    public static class JaxrsJavaResolverProvider implements RestfulResolverProvider {

        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new JaxrsJavaResolver();
        }
    }
}
