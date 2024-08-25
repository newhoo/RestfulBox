package io.github.newhoo.restkit.feature.javaimpl.language;

import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.feature.javaimpl.helper.PsiClassHelper;
import io.github.newhoo.restkit.restful.LanguageResolver;
import io.github.newhoo.restkit.restful.ep.LanguageResolverProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.asJava.LightClassUtil;
import org.jetbrains.kotlin.asJava.LightClassUtilsKt;
import org.jetbrains.kotlin.asJava.classes.KtLightClass;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtClassOrObject;

/**
 * KotlinLanguageResolver, will work when Kotlin enabled
 *
 * @author huzunrong
 * @since 2.0.0
 */
public class KotlinLanguageResolver implements LanguageResolver {

    @NotNull
    @Override
    public Language getLanguage() {
        return KotlinLanguage.INSTANCE;
    }

    @Override
    public boolean canConvertToJSON(@NotNull PsiElement psiElement) {
        return psiElement instanceof KtClassOrObject;
    }

    @Override
    public String convertToJSON(@NotNull PsiElement psiElement) {
        if (psiElement instanceof KtClassOrObject) {
            if (LightClassUtil.INSTANCE.canGenerateLightClass((KtClassOrObject) psiElement)) {
                KtLightClass ktLightClass = LightClassUtilsKt.toLightClass((KtClassOrObject) psiElement);
                if (ktLightClass != null) {
                    return PsiClassHelper.convertClassToJSON(ktLightClass.getQualifiedName(), psiElement.getProject());
                }
            }
        }
        return null;
    }

    public static class KotlinLanguageResolverProvider implements LanguageResolverProvider {

        @NotNull
        @Override
        public LanguageResolver createLanguageResolver() {
            return new KotlinLanguageResolver();
        }
    }
}