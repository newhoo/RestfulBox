package io.github.newhoo.restkit.restful;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import io.github.newhoo.restkit.restful.ep.LanguageResolverProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * LanguageHelper
 *
 * @author huzunrong
 * @since 1.0.0
 */
public class LanguageHelper {

    private final Map<Language, LanguageResolver> resolverMap;

    public LanguageHelper(@NotNull Project project) {
        resolverMap = LanguageResolverProvider.EP_NAME.getExtensionList()
                                                      .stream()
                                                      .filter(Objects::nonNull)
                                                      .map(provider -> provider.createLanguageResolver(project))
                                                      .collect(Collectors.toMap(LanguageResolver::getLanguage, o -> o));
    }

    public static LanguageHelper getInstance(@NotNull Project project) {
        return project.getComponent(LanguageHelper.class);
    }

    public boolean canConvertToJSON(@NotNull PsiElement psiElement) {
        return resolverMap.containsKey(psiElement.getLanguage())
                && resolverMap.get(psiElement.getLanguage()).canConvertToJSON(psiElement);
    }

    public String convertClassToJSON(@NotNull PsiElement psiElement) {
        if (resolverMap.containsKey(psiElement.getLanguage())) {
            return resolverMap.get(psiElement.getLanguage()).convertToJSON(psiElement);
        }
        return null;
    }

    public boolean canNavigateToTree(@NotNull PsiElement psiElement) {
        return resolverMap.containsKey(psiElement.getLanguage())
                && resolverMap.get(psiElement.getLanguage()).canNavigateToTree(psiElement);
    }
}
