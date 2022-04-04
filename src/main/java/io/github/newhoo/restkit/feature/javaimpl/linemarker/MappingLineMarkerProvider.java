package io.github.newhoo.restkit.feature.javaimpl.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import io.github.newhoo.restkit.common.PsiRestItem;
import io.github.newhoo.restkit.common.RestItem;
import io.github.newhoo.restkit.common.ToolkitIcons;
import io.github.newhoo.restkit.config.CommonSettingComponent;
import io.github.newhoo.restkit.feature.javaimpl.MethodPath;
import io.github.newhoo.restkit.feature.javaimpl.language.JavaLanguageResolver;
import io.github.newhoo.restkit.feature.javaimpl.spring.SpringAnnotationHelper;
import io.github.newhoo.restkit.feature.javaimpl.spring.SpringRequestMethodAnnotation;
import io.github.newhoo.restkit.i18n.RestBundle;
import io.github.newhoo.restkit.restful.RequestHelper;
import io.github.newhoo.restkit.toolwindow.RestToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * MappingLineMarkerProvider
 *
 * @author newhoo
 * @date 2022/4/4 10:49
 * @since 2.0.5
 */
public class MappingLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiIdentifier
                && element.getParent() instanceof PsiMethod
                && CommonSettingComponent.getInstance(element.getProject()).getState().isEnableMethodLineMarker()) {
            if (Arrays.stream(((PsiMethod) element.getParent()).getAnnotations())
                      .noneMatch(psiAnnotation -> SpringRequestMethodAnnotation.getByQualifiedName(psiAnnotation.getQualifiedName()) != null)) {
                return null;
            }
            return new LineMarkerInfo<>(element, element.getTextRange(), ToolkitIcons.REQUEST,
                    psiElement -> RestBundle.message("toolkit.navigate.text"),
                    (e, elt) -> {
                        RestToolWindowFactory.getRestServiceToolWindow(elt.getProject(), restServiceToolWindow -> {
                            restServiceToolWindow.navigateToTree(elt.getParent(), new Supplier<RestItem>() {
                                @Override
                                public RestItem get() {
                                    PsiMethod psiMethod = (PsiMethod) element.getParent();
                                    List<MethodPath> typeMethodPaths = SpringAnnotationHelper.getTypeMethodPaths(psiMethod.getContainingClass());
                                    List<MethodPath> methodMethodPaths = SpringAnnotationHelper.getMethodMethodPaths(psiMethod);

                                    return combineTypeAndMethod(typeMethodPaths, methodMethodPaths, psiMethod);
                                }
                            });
                        });
                    },
                    GutterIconRenderer.Alignment.LEFT, () -> RestBundle.message("toolkit.config.name"));
        }
        return null;
    }

    public RestItem combineTypeAndMethod(List<MethodPath> typeMethodPaths, List<MethodPath> methodMethodPaths, PsiElement psiElement) {
        JavaLanguageResolver javaLanguageResolver = new JavaLanguageResolver();
        if (methodMethodPaths.isEmpty()) {
            return null;
        }
        MethodPath methodPath = methodMethodPaths.get(0);
        if (typeMethodPaths.isEmpty()) {
            String requestPath = RequestHelper.getCombinedPath("", methodPath.getPath());
            return new PsiRestItem(requestPath, methodPath.getMethod(), "", javaLanguageResolver.getFrameworkName(), psiElement, javaLanguageResolver);
        } else {
            MethodPath typeMethodPath = typeMethodPaths.get(0);
            String combinedPath = RequestHelper.getCombinedPath(typeMethodPath.getPath(), methodPath.getPath());
            String typeMethod = typeMethodPath.getMethod();

            if (typeMethod != null && !typeMethod.equals(methodPath.getMethod())) {
                return new PsiRestItem(combinedPath, typeMethod, "", javaLanguageResolver.getFrameworkName(), psiElement, javaLanguageResolver);
            }

            return new PsiRestItem(combinedPath, methodPath.getMethod(), "", javaLanguageResolver.getFrameworkName(), psiElement, javaLanguageResolver);
        }
    }
}
