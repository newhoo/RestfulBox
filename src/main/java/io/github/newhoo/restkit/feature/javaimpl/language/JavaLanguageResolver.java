package io.github.newhoo.restkit.feature.javaimpl.language;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiClassHelper;
import io.github.newhoo.restkit.restful.LanguageResolver;
import io.github.newhoo.restkit.restful.ep.LanguageResolverProvider;
import org.jetbrains.annotations.NotNull;

/**
 * JavaLanguageResolver, will work when Java enabled
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class JavaLanguageResolver implements LanguageResolver {

    @NotNull
    @Override
    public Language getLanguage() {
        return JavaLanguage.INSTANCE;
    }

    @Override
    public boolean canConvertToJSON(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiClass && !((PsiClass) psiElement).isAnnotationType();
    }

    @Override
    public String convertToJSON(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiClass) {
            return PsiClassHelper.convertClassToJSON(((PsiClass) psiElement).getQualifiedName(), psiElement.getProject());
        }
        return null;
    }

    public static class JavaLanguageResolverProvider implements LanguageResolverProvider {

        @NotNull
        @Override
        public LanguageResolver createLanguageResolver() {
            return new JavaLanguageResolver();
        }
    }
}
