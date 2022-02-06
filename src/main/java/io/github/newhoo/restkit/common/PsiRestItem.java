package io.github.newhoo.restkit.common;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.restful.ParamResolver;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Restful Api Item from PsiElement which is Navigable
 * <p>
 * ToString(): Read access is allowed from inside read-action (or EDT) only (see com.intellij.openapi.application.Application.runReadAction())
 *
 * @author huzunrong
 */
@Getter
public class PsiRestItem extends RestItem implements Navigatable {

    /**
     * Java: PsiMethod
     * Kotlin: KtNamedFunction
     */
    @NotNull
    private final PsiElement psiElement;
    @NotNull
    private final ParamResolver resolver;

    public PsiRestItem(@NotNull String url, String requestMethod, @NotNull String moduleName, @NotNull String framework, @NotNull PsiElement psiElement, @NotNull ParamResolver resolver) {
        super(url, requestMethod, resolver.buildDescription(psiElement), moduleName, framework);
        this.psiElement = psiElement;
        this.resolver = resolver;
    }

    @NotNull
    @Override
    public List<KV> getHeaders() {
        return resolver.buildHeaders(psiElement);
    }

    @NotNull
    @Override
    public List<KV> getParams() {
        return resolver.buildParams(psiElement);
    }

    @NotNull
    @Override
    public String getBodyJson() {
        return resolver.buildRequestBodyJson(psiElement);
    }

    @Override
    public boolean isValid() {
        return psiElement.isValid();
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public boolean canDelete() {
        return false;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (canNavigate()) {
            ((Navigatable) psiElement).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return psiElement instanceof Navigatable && ((Navigatable) psiElement).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return psiElement instanceof Navigatable && ((Navigatable) psiElement).canNavigateToSource();
    }
}