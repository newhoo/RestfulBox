package io.github.newhoo.restkit.feature.javaimpl.spring;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.config.JavaFilterSetting;
import io.github.newhoo.restkit.intellij.JavaCompactHelper;
import io.github.newhoo.restkit.restful.RequestResolver;
import io.github.newhoo.restkit.restful.ep.RestfulResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring api Java Resolver, will work when Java enabled
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class SpringJavaResolver extends BaseSpringResolver {

    @Override
    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiMethod
                && Arrays.stream(((PsiMethod) psiElement).getAnnotations())
                         .anyMatch(psiAnnotation -> SpringRequestMethodAnnotation.getByQualifiedName(psiAnnotation.getQualifiedName()) != null);
    }

    @Override
    public boolean canGenerateLineMarker(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiIdentifier
                && canNavigateToTree(psiElement.getParent());
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
        List<MethodPath> typeMethodPaths = SpringAnnotationHelper.getTypeMethodPaths(psiMethod.getContainingClass());
        List<MethodPath> methodMethodPaths = SpringAnnotationHelper.getMethodMethodPaths(psiMethod);
        return combineFirstRestItem(typeMethodPaths, methodMethodPaths, psiMethod, "");
    }

    @Override
    public List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope) {
        List<RestItem> itemList = new ArrayList<>();
        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        Set<String> filterClassQualifiedNames = JavaFilterSetting.getInstance().getFilterClassQualifiedNames();
        for (SpringControllerAnnotation controllerAnnotation : supportedAnnotations) {
            // java: 标注了 (Rest)Controller 注解的类，即 Controller 类
            Collection<PsiAnnotation> psiAnnotations = JavaCompactHelper.getAnnotations(controllerAnnotation.getShortName(), module.getProject(), globalSearchScope)
                                                                        .stream()
                                                                        .filter(psiAnnotation -> controllerAnnotation.getQualifiedName().equals(psiAnnotation.getQualifiedName()))
                                                                        .collect(Collectors.toList());
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();

                if (psiElement instanceof PsiClass) {
                    PsiClass psiClass = (PsiClass) psiElement;
                    if (filterClassQualifiedNames.contains(psiClass.getQualifiedName())) {
                        continue;
                    }
                    List<RestItem> serviceItemList = getRequestItemList(psiClass, module);
                    serviceItemList.forEach(e -> e.setPackageName(psiClass.getQualifiedName()));
                    itemList.addAll(serviceItemList);
                }
            }
        }
        return itemList;
    }

    private List<RestItem> getRequestItemList(PsiClass psiClass, Module module) {
        List<PsiMethod> psiMethods = new ArrayList<>(Arrays.asList(psiClass.getMethods()));
        for (PsiClass aSuper : psiClass.getSupers()) {
            if (!"java.lang.Object".equals(aSuper.getQualifiedName())) {
                psiMethods.addAll(Arrays.asList(aSuper.getMethods()));
            }
        }
        if (psiMethods.isEmpty()) {
            return Collections.emptyList();
        }

        List<RestItem> itemList = new ArrayList<>();
        List<MethodPath> typeMethodPaths = SpringAnnotationHelper.getTypeMethodPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            List<MethodPath> methodMethodPaths = SpringAnnotationHelper.getMethodMethodPaths(psiMethod);
            itemList.addAll(combineTypeAndMethod(typeMethodPaths, methodMethodPaths, psiMethod, module));
        }
        return itemList;
    }

    @NotNull
    @Override
    public List<KV> buildHeaders(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return Collections.emptyList();
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        return buildHeaderString(psiMethod);
    }

    @NotNull
    @Override
    public List<KV> buildParams(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return Collections.emptyList();
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        return buildParamString(psiMethod);
    }

    @NotNull
    @Override
    public String buildRequestBodyJson(@NotNull PsiElement psiElement) {
        if (!(psiElement instanceof PsiMethod)) {
            return "";
        }
        PsiMethod psiMethod = (PsiMethod) psiElement;
        String s = buildRequestBodyJson(psiMethod);
        return Objects.nonNull(s) ? s : "";
    }

    @NotNull
    @Override
    public String buildDescription(@NotNull PsiElement psiElement) {
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

    public static class SpringJavaRequestResolverProvider implements RestfulResolverProvider {

        @Override
        public RequestResolver createRequestResolver(@NotNull Project project) {
            return new SpringJavaResolver();
        }
    }
}
