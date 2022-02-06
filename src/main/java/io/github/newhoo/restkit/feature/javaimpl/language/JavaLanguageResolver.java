package io.github.newhoo.restkit.feature.javaimpl.language;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.newhoo.restkit.common.KV;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiClassHelper;
import io.github.newhoo.restkit.feature.javaimpl.spring.SpringAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.spring.SpringControllerAnnotation;
import io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestMethodAnnotation;
import io.github.newhoo.restkit.restful.LanguageResolver;
import io.github.newhoo.restkit.restful.ep.LanguageResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.github.newhoo.restkit.feature.javaimpl.spring.SpringControllerAnnotation.CONTROLLER;

/**
 * JavaLanguageResolver, will work when Java enabled
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class JavaLanguageResolver extends BaseLanguageResolver {

    @NotNull
    @Override
    public Language getLanguage() {
        return JavaLanguage.INSTANCE;
    }

    @Override
    public boolean canConvertToJSON(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiClass;
    }

    @Override
    public String convertToJSON(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            return PsiClassHelper.convertClassToJSON(((PsiClass) psiElement).getQualifiedName(), psiElement.getProject());
        }
        return null;
    }

    @Override
    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiMethod
                && Arrays.stream(((PsiMethod) psiElement).getAnnotations())
                         .anyMatch(psiAnnotation -> SpringRequestMethodAnnotation.getByQualifiedName(psiAnnotation.getQualifiedName()) != null);
    }

    @Override
    public List<RestItem> findRestItemListInModule(Module module, GlobalSearchScope globalSearchScope) {
        List<RestItem> itemList = new ArrayList<>();
        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        for (SpringControllerAnnotation controllerAnnotation : supportedAnnotations) {
            // java: 标注了 (Rest)Controller 注解的类，即 Controller 类
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(controllerAnnotation.getShortName(), module.getProject(), globalSearchScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();

                if (psiElement instanceof PsiClass) {
                    PsiClass psiClass = (PsiClass) psiElement;
                    List<RestItem> serviceItemList = getRequestItemList(psiClass, module);
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
        if (psiMethods.size() == 0) {
            return Collections.emptyList();
        }

        boolean needRequestBody = psiClass.hasAnnotation(CONTROLLER.getQualifiedName())
                && null == PsiAnnotationHelper.getInheritedAnnotation(psiClass, "org.springframework.web.bind.annotation.ResponseBody");

        List<RestItem> itemList = new ArrayList<>();
        List<MethodPath> typeMethodPaths = SpringAnnotationHelper.getTypeMethodPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            if (needRequestBody
                    && PsiAnnotationHelper.getInheritedAnnotation(psiMethod, "org.springframework.web.bind.annotation.ResponseBody") == null) {
                continue;
            }

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

        String restName = null;
        String location;
        if (psiMethod.getDocComment() != null) {
            restName = Arrays.stream(psiMethod.getDocComment().getDescriptionElements())
                             .filter(e -> e instanceof PsiDocToken)
                             .filter(e -> StringUtils.isNotBlank(e.getText()))
                             .findFirst()
                             .map(e -> e.getText().trim()).orElse(null);
        }
        location = psiMethod.getContainingClass().getName().concat("#").concat(psiMethod.getName());
        if (StringUtils.isNotEmpty(restName)) {
            location = location.concat("#").concat(restName);
        }
        return location;
    }

    public static class JavaLanguageResolverProvider implements LanguageResolverProvider {

        @NotNull
        @Override
        public LanguageResolver createLanguageResolver() {
            return new JavaLanguageResolver();
        }
    }
}
